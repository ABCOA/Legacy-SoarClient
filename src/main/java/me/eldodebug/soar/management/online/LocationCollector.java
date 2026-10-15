package me.eldodebug.soar.management.online;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.HypixelModAPIImplementation;
import net.hypixel.modapi.packet.HypixelPacket;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.hypixel.modapi.serializer.PacketSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.EnumChatFormatting;

import java.util.Locale;

public final class LocationCollector {
    public static final class Location {
        private final String network, server, game, lobby, mode, map;
        Location(String network, String server, String game, String lobby, String mode, String map) {
            this.network = network; this.server = server; this.game = game;
            this.lobby = lobby; this.mode = mode; this.map = map;
        }
        public String getNetwork() { return network; }
        public String getServer() { return server; }
        public String getGame() { return game; }
        public String getLobby() { return lobby; }
        public String getMode() { return mode; }
        public String getMap() { return map; }
        public boolean isKnown() { return "hypixel".equals(network) && !server.isEmpty(); }
    }
    private static final LocationCollector INSTANCE = new LocationCollector();
    public static LocationCollector getInstance() { return INSTANCE; }
    private final Minecraft mc = Minecraft.getMinecraft();
    private Location location = new Location("", "", "", "", "", "");
    private long nextQuery, pendingUntil, officialUntil;
    private boolean ownsImplementation;
    private boolean initialized;
    private NetworkManager helloConnection;
    private NetworkManager provenHypixelConnection, queryConnection;
    private long queryEpoch;
    private boolean suppressPendingReply, automaticQuerySending, acceptPendingLocation;
    private int unavailableReplies;
    private long epoch;
    private LocationCollector() { }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        HypixelModAPI api = HypixelModAPI.getInstance();
        try {
            api.setModImplementation(new HypixelModAPIImplementation() {
                @Override public void onInit() { }
                @Override public boolean isConnectedToHypixel() {
                    return isHypixel() && mc.getNetHandler() != null
                            && mc.getNetHandler().getNetworkManager() == helloConnection;
                }
                @Override public boolean sendPacket(HypixelPacket packet) {
                    if (!isConnectedToHypixel()) return false;
                    ByteBuf buffer = Unpooled.buffer();
                    try {
                        packet.write(new PacketSerializer(buffer));
                        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(packet.getIdentifier(), new PacketBuffer(buffer)));
                        return true;
                    } catch (RuntimeException e) { buffer.release(); return false; }
                }
            });
            ownsImplementation = true;
        } catch (IllegalStateException existingMod) {
            ownsImplementation = false;
        }
        api.createHandler(ClientboundLocationPacket.class, packet -> {
            long expected = epoch;
            Runnable apply = () -> {
                if (!isHypixel() || expected != epoch) return;
                acceptOfficial(packet.getServerName(), packet.getServerType().map(type -> type.name()).orElse(""),
                        packet.getLobbyName().orElse(""), packet.getMode().orElse(""), packet.getMap().orElse(""));
            };
            if (mc.isCallingFromMinecraftThread()) apply.run(); else mc.addScheduledTask(apply);
        });
        api.subscribeToEventPacket(ClientboundLocationPacket.class);
    }
    public boolean isHypixel() { return "hypixel".equals(network()); }
    private NetworkManager connection() {
        return mc.getNetHandler() == null ? null : mc.getNetHandler().getNetworkManager();
    }
    private String serverHost() {
        String ip = mc.getCurrentServerData() == null ? "" : mc.getCurrentServerData().serverIP;
        String host = ip.toLowerCase(Locale.ROOT).trim();
        int colon = host.lastIndexOf(':');
        if (colon > 0 && host.indexOf(':') == colon) host = host.substring(0, colon);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host;
    }
    public String network() {
        NetworkManager current = connection();
        if (current != null && current.isChannelOpen()
                && (current == helloConnection || current == provenHypixelConnection)) return "hypixel";
        String host = serverHost();
        if ("hypixel.net".equals(host) || host.endsWith(".hypixel.net")) return "hypixel";
        return host.isEmpty() ? "" : "other:" + host;
    }
    private boolean mayQueryHypixel() {
        String brand = mc.thePlayer == null ? null : mc.thePlayer.getClientBrand();
        return isHypixel() || serverHost().matches("(?:.*\\.)?hypixel(?:[.\\-].*)?")
                || brand != null && brand.toLowerCase(Locale.ROOT).contains("hypixel");
    }
    public void worldChanged() {
        epoch++;
        location = new Location(network(), "", "", "", "", "");
        long now = System.currentTimeMillis(); nextQuery = now + 3000; pendingUntil = 0; officialUntil = 0;
        queryConnection = null; suppressPendingReply = false; automaticQuerySending = false;
        acceptPendingLocation = false; unavailableReplies = 0;
    }
    public void leave() { helloConnection = null; provenHypixelConnection = null; worldChanged(); }
    public void tick() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.isSingleplayer()) return;
        String net = network();
        if (!net.equals(location.network)) worldChanged();
        long now = System.currentTimeMillis();
        if (mayQueryHypixel() && now >= nextQuery && now >= officialUntil && now >= pendingUntil) {
            nextQuery = now + (location.isKnown() ? 60000 : 15000);
            automaticQuerySending = true;
            try { mc.thePlayer.sendChatMessage("/locraw"); }
            finally { automaticQuerySending = false; }
        }
    }
    public void observeOutgoingChat(String message) {
        if (message == null || !"/locraw".equalsIgnoreCase(message.trim()) || mc.theWorld == null
                || mc.thePlayer == null || mc.isSingleplayer() || connection() == null) return;
        beginQuery(automaticQuerySending);
    }
    private void beginQuery(boolean automatic) {
        long now = System.currentTimeMillis();
        queryConnection = connection(); queryEpoch = epoch; pendingUntil = now + 15000;
        suppressPendingReply = automatic; acceptPendingLocation = true;
        nextQuery = now + (automatic && !location.isKnown() ? 15000 : 60000);
    }
    private void clearQuery() {
        pendingUntil = 0; queryConnection = null; suppressPendingReply = false; acceptPendingLocation = false;
    }
    private void deferUnavailableQuery(long now) {
        unavailableReplies = Math.min(unavailableReplies + 1, 4);
        nextQuery = now + Math.min(300000L, 60000L << (unavailableReplies - 1));
    }
    public void acceptOfficial(String server, String game, String lobby, String mode, String map) {
        if (!isHypixel()) return;
        location = new Location("hypixel", safeServer(server), safe(game), safe(lobby), safe(mode), safe(map));
        long now = System.currentTimeMillis();
        officialUntil = location.isKnown() ? now + 120000 : 0;
        if (location.isKnown()) {
            acceptPendingLocation = false; unavailableReplies = 0;
        } else if ("limbo".equalsIgnoreCase(server)) {
            acceptPendingLocation = false;
            deferUnavailableQuery(now);
        } else nextQuery = Math.min(nextQuery, now + 3000);
    }
    public boolean receiveChat(String message) {
        if (message == null || message.length() > 4096 || queryEpoch != epoch
                || queryConnection == null || queryConnection != connection() || !queryConnection.isChannelOpen()
                || System.currentTimeMillis() >= pendingUntil) return false;
        String raw = EnumChatFormatting.getTextWithoutFormattingCodes(message).trim();
        if (!raw.startsWith("{") || !raw.endsWith("}")) return false;
        try {
            JsonObject json = new JsonParser().parse(raw).getAsJsonObject();
            if (!json.has("server")) return false;
            String rawServer = string(json, "server"), game = string(json, "gametype");
            String lobby = string(json, "lobbyname"), mode = string(json, "mode"), map = string(json, "map");
            boolean unavailable = "limbo".equalsIgnoreCase(rawServer) || "unknown".equalsIgnoreCase(rawServer)
                    || "null".equalsIgnoreCase(rawServer);
            String server = safeServer(rawServer);
            if (!unavailable && (server.isEmpty() || game.isEmpty())) return false;
            boolean suppress = suppressPendingReply;
            boolean apply = acceptPendingLocation;
            long now = System.currentTimeMillis();
            if (unavailable) {
                clearQuery();
                if (apply) {
                    location = new Location(network(), "", "", "", "", "");
                    officialUntil = 0;
                    deferUnavailableQuery(now);
                }
                return suppress;
            }
            Location received = new Location("hypixel", server, game, lobby, mode, map);
            if (apply) {
                provenHypixelConnection = queryConnection;
                location = received;
                unavailableReplies = 0;
            }
            clearQuery();
            if (apply) nextQuery = now + 60000;
            return suppress;
        } catch (RuntimeException invalid) { return false; }
    }
    public void receivePayload(String channel, byte[] bytes) {
        if (bytes == null || bytes.length > 32767 || connection() == null) return;
        if (!("hypixel:hello".equals(channel) || "hyevent:location".equals(channel))) return;
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            if ("hypixel:hello".equals(channel)) {
                PacketSerializer serializer = new PacketSerializer(buffer);
                if (!serializer.readBoolean()) return;
                new ClientboundHelloPacket(serializer);
                buffer.readerIndex(0);
                helloConnection = connection();
            }
            if (ownsImplementation && isHypixel()) HypixelModAPI.getInstance().handle(channel, new PacketSerializer(buffer));
        }
        catch (RuntimeException ignored) { }
        finally { buffer.release(); }
    }
    public Location getLocation() { return location; }
    private static String safe(String value) { return value == null || value.length() > 128 ? "" : value; }
    private static String safeServer(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}") && !"unknown".equalsIgnoreCase(value)
                && !"null".equalsIgnoreCase(value) && !"limbo".equalsIgnoreCase(value) ? value : "";
    }
    private static String string(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) return "";
        if (!object.get(key).isJsonPrimitive() || !object.get(key).getAsJsonPrimitive().isString())
            throw new IllegalArgumentException("Location fields must be strings");
        return safe(object.get(key).getAsString());
    }
}
