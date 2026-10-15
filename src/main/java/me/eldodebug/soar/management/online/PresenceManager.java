package me.eldodebug.soar.management.online;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Session;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class PresenceManager implements PresenceTransport.Listener {
    public interface UploadCallback {
        void onUploaded(String canonicalHash);
        void onFailure(String reason);
    }
    static final String DEFAULT_ENDPOINT = "wss://online.soarclient.net/v1/presence";
    private static volatile PresenceManager instance;
    public static PresenceManager getInstance() { return instance; }
    private final Minecraft mc = Minecraft.getMinecraft();
    private final OnlineSettings settings;
    private final URI endpoint;
    private final String httpOrigin;
    private final PresenceTransport transport;
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4), runnable -> {
                Thread thread = new Thread(runnable, "Soar Presence Worker"); thread.setDaemon(true); return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
    private final Map<UUID, OnlineMember> visibleMembers = new HashMap<UUID, OnlineMember>();
    private Map<UUID, OnlineMember> roster = Collections.emptyMap();
    private Set<UUID> visibleUuids = Collections.emptySet();
    private CapeDescriptor cape = CapeDescriptor.official();
    private long generation, locationGeneration, nextAttempt, lastReceived, nextHeartbeat, nextVisible;
    private long roomVersion = -1, noticeAt;
    private int retry, count;
    private String identity = "", sentNetwork = "", sentServer = "", error = "";
    private boolean authenticated, connecting, stopped;
    private boolean proving;
    private volatile MojangJoins.Ticket joinTicket;
    private boolean accountLinkAvailable;
    private AccountLinkPending accountLinkPending;
    private AccountLinkPreview accountLinkPreview;
    private TranslateText accountLinkStatus;
    private String[] accountLinkStatusArguments = new String[0];
    private UploadPending upload;
    private long nextUploadAllowed;
    private boolean notifyRoom;
    private int lastNotifiedCount;
    private final Set<UUID> notifiedUsers = new HashSet<UUID>();

    public PresenceManager(File directory) {
        instance = this;
        settings = new OnlineSettings(directory);
        endpoint = endpoint(System.getProperty("soar.online.endpoint", DEFAULT_ENDPOINT));
        httpOrigin = ("wss".equals(endpoint.getScheme()) ? "https" : "http") + "://" + endpoint.getRawAuthority();
        transport = new PresenceTransport(this);
        LocationCollector.getInstance().initialize();
    }
    static URI endpoint(String value) {
        URI uri = URI.create(value);
        boolean secure = "wss".equals(uri.getScheme());
        boolean loopback = "ws".equals(uri.getScheme()) && ("localhost".equals(uri.getHost())
                || "127.0.0.1".equals(uri.getHost()) || "::1".equals(uri.getHost()));
        if ((!secure && !loopback) || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null)
            throw new IllegalArgumentException("Presence endpoint must use WSS (WS is allowed only on loopback)");
        return uri;
    }
    private boolean gameReady() {
        return !stopped && mc.theWorld != null && mc.thePlayer != null && !mc.isSingleplayer()
                && mc.getNetHandler() != null && mc.getNetHandler().getNetworkManager().isChannelOpen() && mc.getSession() != null;
    }
    public void onTick() {
        if (!gameReady()) { if (connecting || authenticated || !identity.isEmpty()) onLeave(); return; }
        long now = System.currentTimeMillis();
        Session session = mc.getSession();
        String currentIdentity = session.getPlayerID() + ":" + session.getUsername();
        if (!currentIdentity.equals(identity)) { disconnect(false); identity = currentIdentity; nextAttempt = now; }
        LocationCollector.getInstance().tick();
        if (!settings.noticeSeen) {
            print(TranslateText.ONLINE_SHARED_NOTICE.getText()); settings.noticeSeen = true; settings.save();
        }
        if (!connecting && !authenticated && now >= nextAttempt) connect();
        if (connecting || authenticated) {
            if (now - lastReceived > 90000) { fail(generation, "Service response timed out"); return; }
        }
        if (authenticated) {
            publishLocation();
            if (now >= nextHeartbeat) { send(message("ping")); nextHeartbeat = now + 30000; }
            if (now >= nextVisible) { publishVisible(); nextVisible = now + 3000; }
        }
        if (upload != null && now > upload.deadline) finishUploadFailure(upload, "Upload permission timed out");
        if (accountLinkPending != null && System.nanoTime() - accountLinkPending.deadlineNanos >= 0) {
            accountLinkPending = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_TIMEOUT); print(getAccountLinkStatus());
        }
        if (accountLinkPreview != null && accountLinkPreview.isExpired()) {
            accountLinkPreview = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_EXPIRED);
        }
        if (notifyRoom && now >= noticeAt) {
            notifyRoom = false;
            if (count > 0) printRoomNotice();
            lastNotifiedCount = count; notifiedUsers.clear(); notifiedUsers.addAll(roster.keySet());
        }
    }
    private void connect() {
        generation++; connecting = true; authenticated = false; error = "";
        lastReceived = System.currentTimeMillis(); nextHeartbeat = lastReceived + 30000;
        transport.connect(endpoint, generation);
    }
    public void onWorldChanged(WorldClient world) {
        disconnect(false);
        LocationCollector.getInstance().worldChanged();
        nextAttempt = System.currentTimeMillis() + 1000;
        if (world == null) { identity = ""; LocationCollector.getInstance().leave(); }
    }
    public void onLeave() {
        disconnect(false); identity = ""; nextAttempt = 0; retry = 0;
        LocationCollector.getInstance().leave();
    }
    public void shutdown() {
        stopped = true; disconnect(false); transport.shutdown(); workers.shutdownNow(); settings.save();
    }
    private void disconnect(boolean keepError) {
        generation++; connecting = false; authenticated = false; proving = false; releaseJoin(); transport.close();
        accountLinkAvailable = false; accountLinkPending = null; accountLinkPreview = null; accountLinkStatus = null; accountLinkStatusArguments = new String[0];
        clearRoom(); sentNetwork = ""; sentServer = ""; nextVisible = 0;
        if (upload != null) finishUploadFailure(upload, "Online session changed");
        if (!keepError) error = "";
    }
    private void releaseJoin() {
        MojangJoins.Ticket held = joinTicket; joinTicket = null;
        if (held != null) held.release();
    }
    private void clearRoom() {
        roster = Collections.emptyMap(); visibleMembers.clear(); visibleUuids = Collections.emptySet();
        count = 0; roomVersion = -1; notifyRoom = false; lastNotifiedCount = 0; notifiedUsers.clear();
    }
    private void fail(long expected, String reason) {
        if (expected != generation || stopped) return;
        disconnect(true); error = reason;
        retry = Math.min(6, retry + 1);
        nextAttempt = System.currentTimeMillis() + Math.min(60000, 1000L << retry) + new Random().nextInt(1000);
    }
    @Override public void onClosed(long expected, String reason) {
        mc.addScheduledTask(() -> fail(expected, reason));
    }
    @Override public void onText(long expected, String text) {
        if (text == null || text.length() > 65536) { onClosed(expected, "Server response too large"); return; }
        mc.addScheduledTask(() -> {
            if (expected != generation || !gameReady()) return;
            try {
                JsonObject json = new JsonParser().parse(text).getAsJsonObject();
                receive(expected, json); lastReceived = System.currentTimeMillis();
            } catch (RuntimeException malformed) { fail(expected, "Invalid server response"); }
        });
    }
    private void receive(long expected, JsonObject json) {
        String type = string(json, "type");
        if ("challenge".equals(type) && connecting && !authenticated && !proving) {
            if (json.get("version").getAsInt() != 1 || !string(json, "serverId").matches("[a-fA-F0-9]{40}"))
                throw new IllegalArgumentException("Unsupported authentication challenge");
            Session session = mc.getSession(); GameProfile profile = session.getProfile();
            String token = session.getToken(), challenge = string(json, "serverId");
            proving = true;
            try { workers.execute(() -> {
                try {
                    MojangJoins.Ticket ticket = MojangJoins.acquire(10000);
                    if (ticket == null) { onClosed(expected, "Minecraft authentication is busy"); return; }
                    joinTicket = ticket;
                    if (expected != generation) { releaseJoin(); return; }
                    mc.getSessionService().joinServer(profile, token, challenge);
                    mc.addScheduledTask(() -> {
                        if (expected != generation || !gameReady()) return;
                        JsonObject auth = message("authenticate"); auth.addProperty("username", profile.getName());
                        auth.addProperty("hidden", settings.hidden); send(auth);
                    });
                } catch (Exception rejected) { onClosed(expected, "Minecraft account authentication failed"); }
            }); } catch (RejectedExecutionException busy) { fail(expected, "Authentication worker is busy"); }
        } else if ("authenticated".equals(type) && connecting) {
            UUID verified = UUID.fromString(string(json, "uuid"));
            if (!verified.equals(mc.getSession().getProfile().getId())) throw new IllegalArgumentException("Account mismatch");
            connecting = false; authenticated = true; proving = false; retry = 0; releaseJoin();
            accountLinkAvailable = false;
            if (json.has("capabilities") && json.get("capabilities").isJsonArray()) {
                JsonArray capabilities = json.getAsJsonArray("capabilities");
                if (capabilities.size() <= 32) for (JsonElement capability : capabilities)
                    if (capability.isJsonPrimitive() && capability.getAsJsonPrimitive().isString()
                            && "account_link_v1".equals(capability.getAsString())) accountLinkAvailable = true;
            }
            JsonObject privacy = message("privacy"); privacy.addProperty("hidden", settings.hidden); send(privacy);
            publishLocation();
            if (Soar.getInstance().getCapeManager() != null) Soar.getInstance().getCapeManager().syncCurrentSelection();
            publishCape(); publishVisible();
        } else if (("room".equals(type) || "room_delta".equals(type)) && authenticated) {
            if (!currentScope(json)) return;
            long version = json.get("version").getAsLong();
            if (version <= roomVersion) return;
            roomVersion = version;
            Map<UUID, OnlineMember> next = "room_delta".equals(type) ? new LinkedHashMap<UUID, OnlineMember>(roster)
                    : new LinkedHashMap<UUID, OnlineMember>();
            if ("room_delta".equals(type)) {
                JsonArray left = json.getAsJsonArray("left");
                if (left.size() > 1000) throw new IllegalArgumentException("Delta too large");
                for (JsonElement value : left) next.remove(UUID.fromString(value.getAsString()));
            }
            JsonArray members = json.getAsJsonArray("room_delta".equals(type) ? "joined" : "members");
            if (members.size() > 1000) throw new IllegalArgumentException("Roster too large");
            for (JsonElement element : members) {
                JsonObject member = element.getAsJsonObject(); String username = string(member, "username");
                if (!username.matches("[A-Za-z0-9_]{1,16}")) continue;
                UUID uuid = UUID.fromString(string(member, "uuid"));
                next.put(uuid, new OnlineMember(uuid, username, null));
            }
            int nextCount = Math.max(0, Math.min(100000, json.get("count").getAsInt()));
            boolean added = !notifiedUsers.containsAll(next.keySet()) || nextCount > lastNotifiedCount;
            roster = Collections.unmodifiableMap(next); count = nextCount;
            if (added && count > 0) { notifyRoom = true; noticeAt = System.currentTimeMillis() + 1200; }
        } else if ("players".equals(type) && authenticated) {
            if (!currentScope(json)) return;
            JsonArray players = json.getAsJsonArray("players");
            if (players.size() > 80) throw new IllegalArgumentException("Visible response too large");
            for (JsonElement element : players) {
                JsonObject player = element.getAsJsonObject(); UUID uuid = UUID.fromString(string(player, "uuid"));
                if (!visibleUuids.contains(uuid)) continue;
                if (!player.get("online").getAsBoolean()) { visibleMembers.remove(uuid); continue; }
                OnlineMember publicMember = roster.get(uuid);
                visibleMembers.put(uuid, new OnlineMember(uuid, publicMember == null ? "" : publicMember.getUsername(),
                        CapeDescriptor.fromJson(player.getAsJsonObject("cape"))));
            }
        } else if ("account_link_preview".equals(type) && authenticated) {
            if (accountLinkPending == null || accountLinkPending.confirm || !accountLinkPending.id.equals(string(json, "requestId"))) return;
            String token = bindingString(json, "confirmToken"), hint = bindingString(json, "accountHint"), idHint = bindingString(json, "accountIdHint"), name = bindingString(json, "minecraftName");
            long expires = positiveInteger(json, "expiresAt", Long.MAX_VALUE);
            long lifetime = json.has("expiresInMs") ? positiveInteger(json, "expiresInMs", 60000) : 60000;
            if (!token.matches("[A-Za-z0-9_-]{22,128}") || expires < 0 || lifetime < 0
                    || hint.isEmpty() || hint.length() > 128 || idHint.isEmpty() || idHint.length() > 64
                    || hint.indexOf('\n') >= 0 || hint.indexOf('\r') >= 0 || idHint.indexOf('\n') >= 0 || idHint.indexOf('\r') >= 0
                    || !name.equalsIgnoreCase(mc.getSession().getUsername())) {
                accountLinkPending = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_INVALIDPREVIEW); print(getAccountLinkStatus()); return;
            }
            long waited = Math.max(0, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - accountLinkPending.startedNanos));
            long remaining = lifetime - waited;
            if (remaining <= 0) {
                accountLinkPending = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_EXPIRED); print(getAccountLinkStatus()); return;
            }
            accountLinkPreview = new AccountLinkPreview(accountLinkPending.id, token, hint, idHint, name, expires,
                    System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(remaining)); accountLinkPending = null;
            setAccountLinkStatus(TranslateText.ONLINE_LINK_REVIEW);
            print(String.format(Locale.ROOT, TranslateText.ONLINE_LINK_TARGET.getText(), hint));
            print(String.format(Locale.ROOT, TranslateText.ONLINE_LINK_ACCOUNTID.getText(), idHint));
            print(String.format(Locale.ROOT, TranslateText.ONLINE_LINK_PLAYER.getText(), name));
            print(getAccountLinkStatus());
            print(TranslateText.ONLINE_LINK_COMMANDS.getText());
        } else if ("account_link_result".equals(type) && authenticated) {
            if (accountLinkPending == null || !accountLinkPending.confirm || !accountLinkPending.id.equals(string(json, "requestId"))) return;
            if (!json.has("linked") || !json.get("linked").getAsBoolean()
                    || !mc.getSession().getProfile().getId().toString().equals(string(json, "uuid"))
                    || !mc.getSession().getUsername().equalsIgnoreCase(string(json, "username"))) {
                accountLinkPending = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_INVALIDRESULT); print(getAccountLinkStatus()); return;
            }
            accountLinkPending = null; accountLinkPreview = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_SUCCESS); print(getAccountLinkStatus());
        } else if ("upload_ticket".equals(type) && authenticated) {
            if (upload == null || !upload.id.equals(string(json, "requestId"))) return;
            if (json.has("hash")) {
                String canonical = string(json, "hash");
                if (!canonical.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid existing cape hash");
                UploadPending pending = upload; upload = null; pending.callback.onUploaded(canonical); return;
            }
            String ticket = string(json, "ticket");
            if (!ticket.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid upload permission");
            UploadPending pending = upload;
            long expiresAt = json.get("expiresAt").getAsLong();
            if (expiresAt <= System.currentTimeMillis()) { finishUploadFailure(pending, "Upload permission expired"); return; }
            pending.deadline = Math.min(expiresAt, System.currentTimeMillis() + 20000);
            try { workers.execute(() -> postCape(pending, ticket)); }
            catch (RejectedExecutionException busy) { finishUploadFailure(pending, "Upload worker is busy"); }
        } else if ("error".equals(type)) {
            String code = string(json, "code");
            if (code.startsWith("ACCOUNT_LINK_")) {
                if (accountLinkPending != null && accountLinkPending.id.equals(string(json, "requestId"))) {
                    accountLinkPending = null; setAccountLinkStatus(accountLinkError(code));
                    if (code.contains("EXPIRED") || code.contains("INVALID") || code.contains("USED")) accountLinkPreview = null;
                    print(getAccountLinkStatus());
                }
                return;
            }
            error = code;
            if (upload != null && upload.id.equals(string(json, "requestId"))) {
                if (json.has("retryAfter")) nextUploadAllowed = System.currentTimeMillis() + Math.min(86400,
                        Math.max(1, json.get("retryAfter").getAsLong())) * 1000;
                finishUploadFailure(upload, string(json, "message"));
            } else if (("CAPE_NOT_FOUND".equals(code) || "UNKNOWN_CAPE".equals(code)) && "custom".equals(cape.getType())
                    && Soar.getInstance().getCapeManager() != null) {
                Soar.getInstance().getCapeManager().onPublishedCapeRejected(cape.getHash());
            } else if (!authenticated || "AUTH_FAILED".equals(code) || "AUTHENTICATION".equals(code)) fail(expected, code);
        }
    }
    private boolean currentScope(JsonObject json) {
        return json.has("generation") && json.get("generation").getAsLong() == locationGeneration
                && json.has("network") && sentNetwork.equals(string(json, "network"))
                && json.has("server") && sentServer.equals(string(json, "server"));
    }
    private void publishLocation() {
        LocationCollector.Location location = LocationCollector.getInstance().getLocation();
        String network = location.getNetwork(), server = location.getServer();
        if (network.equals(sentNetwork) && server.equals(sentServer)) return;
        clearRoom(); sentNetwork = network; sentServer = server; locationGeneration++;
        JsonObject json = message("location"); json.addProperty("network", network);
        json.addProperty("server", server); json.addProperty("generation", locationGeneration); send(json);
        nextVisible = 0;
    }
    private void publishVisible() {
        LinkedHashSet<UUID> next = new LinkedHashSet<UUID>();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (next.size() >= 80) break; next.add(player.getUniqueID());
        }
        for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
            if (next.size() >= 80) break; if (info.getGameProfile().getId() != null) next.add(info.getGameProfile().getId());
        }
        if (next.equals(visibleUuids) && !visibleUuids.isEmpty()) return;
        visibleMembers.keySet().retainAll(next); visibleUuids = Collections.unmodifiableSet(next);
        JsonObject json = message("visible"); JsonArray uuids = new JsonArray();
        for (UUID uuid : next) uuids.add(new JsonPrimitive(uuid.toString()));
        json.add("uuids", uuids); json.addProperty("generation", locationGeneration); send(json);
    }
    public void setCape(CapeDescriptor descriptor) {
        cape = descriptor == null ? CapeDescriptor.official() : descriptor;
        if (authenticated) publishCape();
    }
    private void publishCape() { JsonObject json = cape.toJson(); json.addProperty("type", "cape"); send(json); }
    public boolean isOnline(UUID uuid) {
        return authenticated && uuid != null && (visibleMembers.containsKey(uuid) || roster.containsKey(uuid)
                || uuid.equals(mc.getSession().getProfile().getId()));
    }
    public OnlineMember getMember(UUID uuid) {
        if (!authenticated) return null;
        OnlineMember member = visibleMembers.get(uuid);
        return member == null ? roster.get(uuid) : member;
    }
    public Map<UUID, OnlineMember> getRoster() { return roster; }
    public int getCount() { return count; }
    public int getHiddenCount() { return Math.max(0, count - roster.size()); }
    public boolean isAuthenticated() { return authenticated; }
    public boolean isUsernameHidden() { return settings.hidden; }
    public void setUsernameHidden(boolean value) {
        settings.hidden = value; settings.save();
        if (authenticated) { JsonObject json = message("privacy"); json.addProperty("hidden", value); send(json); }
    }
    public String getStatusText() {
        if (!gameReady()) return TranslateText.ONLINE_WAITING.getText();
        if (authenticated) {
            LocationCollector.Location location = LocationCollector.getInstance().getLocation();
            return TranslateText.ONLINE_CONNECTED.getText() + (location.isKnown() ? " | " + location.getServer() : "");
        }
        if (connecting) return TranslateText.ONLINE_CONNECTING.getText();
        return TranslateText.ONLINE_OFFLINE.getText() + (error.isEmpty() ? "" : " (" + error + ")");
    }
    public void printRoster() {
        if (!authenticated) { print(getStatusText()); return; }
        LocationCollector.Location location = LocationCollector.getInstance().getLocation();
        if (!location.isKnown()) { print("当前尚未获取可靠的小服编号，暂时无法获取同小服名单。"); return; }
        print(countText(TranslateText.ONLINE_ROSTER, count));
        if (!roster.isEmpty()) {
            List<String> names = new ArrayList<String>();
            for (OnlineMember member : roster.values()) names.add(member.getUsername());
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
            for (int start = 0; start < names.size(); start += 12) print(String.join(", ", names.subList(start, Math.min(names.size(), start + 12))));
        }
        if (getHiddenCount() > 0) print(countText(TranslateText.ONLINE_HIDDEN_COUNT, getHiddenCount()));
    }
    private void printRoomNotice() {
        print(countText(TranslateText.ONLINE_ROSTER, count));
        if (!roster.isEmpty()) {
            List<String> names = new ArrayList<String>();
            for (OnlineMember member : roster.values()) names.add(member.getUsername());
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
            print(String.join(", ", names.subList(0, Math.min(8, names.size()))) + (names.size() > 8 ? " … (.online list)" : ""));
        }
        if (getHiddenCount() > 0) print(countText(TranslateText.ONLINE_HIDDEN_COUNT, getHiddenCount()));
    }
    private static String countText(TranslateText text, int value) {
        String label = text.getText();
        return label.contains("%s") ? label.replace("%s", Integer.toString(value)) : label + ": " + value;
    }
    public void print(String text) {
        if (mc.ingameGUI != null) mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§b[Soar Online] §r" + text));
    }
    public boolean onGameChat(String text) { return LocationCollector.getInstance().receiveChat(text); }
    public void onCustomPayload(String channel, byte[] bytes) {
        if (bytes == null || bytes.length > 32767) return;
        byte[] copy = Arrays.copyOf(bytes, bytes.length); long expected = generation;
        Runnable apply = () -> {
            if (expected == generation && gameReady()) LocationCollector.getInstance().receivePayload(channel, copy);
        };
        if (mc.isCallingFromMinecraftThread()) apply.run(); else mc.addScheduledTask(apply);
    }
    private static JsonObject message(String type) { JsonObject json = new JsonObject(); json.addProperty("type", type); return json; }
    private void send(JsonObject json) { transport.send(json.toString()); }
    private static String string(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }
    private static String bindingString(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : "";
    }
    private static long positiveInteger(JsonObject json, String key, long maximum) {
        JsonElement value = json.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) return -1;
        String number = value.getAsString();
        if (!number.matches("[0-9]{1,19}")) return -1;
        try { long parsed = Long.parseLong(number); return parsed > 0 && parsed <= maximum ? parsed : -1; }
        catch (NumberFormatException invalid) { return -1; }
    }
    public boolean canLinkAccount() { return authenticated && accountLinkAvailable && gameReady(); }
    public boolean isAccountLinkBusy() { return accountLinkPending != null; }
    public AccountLinkPreview getAccountLinkPreview() { return accountLinkPreview != null && !accountLinkPreview.isExpired() ? accountLinkPreview : null; }
    public String getAccountLinkStatus() {
        if (!authenticated || !gameReady()) return TranslateText.ONLINE_LINK_AUTHREQUIRED.getText();
        if (!accountLinkAvailable) return TranslateText.ONLINE_LINK_UNAVAILABLE.getText();
        return accountLinkStatus == null ? TranslateText.ONLINE_LINK_HINT.getText()
                : String.format(Locale.ROOT, accountLinkStatus.getText(), (Object[]) accountLinkStatusArguments);
    }
    public void previewAccountLink(String rawCode) {
        if (!canLinkAccount()) { print(getAccountLinkStatus()); return; }
        if (accountLinkPending != null) { print(TranslateText.ONLINE_LINK_BUSY.getText()); return; }
        String code = rawCode == null ? "" : rawCode.trim().replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
        if (!code.matches("[0-9A-HJKMNP-TV-Z]{12}")) { setAccountLinkStatus(TranslateText.ONLINE_LINK_INVALIDCODE); print(getAccountLinkStatus()); return; }
        accountLinkPreview = null;
        String id = UUID.randomUUID().toString(); accountLinkPending = new AccountLinkPending(id, false, 10000);
        setAccountLinkStatus(TranslateText.ONLINE_LINK_PREVIEWING);
        JsonObject request = message("account_link_preview"); request.addProperty("requestId", id); request.addProperty("code", code); send(request);
    }
    public void confirmAccountLink() {
        if (!canLinkAccount()) { print(getAccountLinkStatus()); return; }
        if (accountLinkPending != null) { print(TranslateText.ONLINE_LINK_BUSY.getText()); return; }
        AccountLinkPreview preview = getAccountLinkPreview();
        if (preview == null) { accountLinkPreview = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_CONFIRMREQUIRED); print(getAccountLinkStatus()); return; }
        accountLinkPending = new AccountLinkPending(preview.requestId, true, 10000);
        setAccountLinkStatus(TranslateText.ONLINE_LINK_CONFIRMING);
        JsonObject request = message("account_link_confirm"); request.addProperty("requestId", preview.requestId); request.addProperty("confirmToken", preview.confirmToken); send(request);
    }
    public void cancelAccountLink() {
        if (accountLinkPending != null && accountLinkPending.confirm) { print(TranslateText.ONLINE_LINK_CONFIRMSENT.getText()); return; }
        accountLinkPending = null; accountLinkPreview = null; setAccountLinkStatus(TranslateText.ONLINE_LINK_CANCELLED); print(getAccountLinkStatus());
    }
    private void setAccountLinkStatus(TranslateText text, String... arguments) {
        accountLinkStatus = text; accountLinkStatusArguments = arguments;
    }
    private static TranslateText accountLinkError(String code) {
        if (code.contains("RATE") || code.contains("BUSY")) return TranslateText.ONLINE_LINK_RATELIMIT;
        if (code.contains("ALREADY") || code.contains("CONFLICT")) return TranslateText.ONLINE_LINK_CONFLICT;
        if (code.contains("EXPIRED") || code.contains("INVALID") || code.contains("USED")) return TranslateText.ONLINE_LINK_EXPIRED;
        if (code.contains("AUTH") || code.contains("SESSION")) return TranslateText.ONLINE_LINK_SESSIONCHANGED;
        return TranslateText.ONLINE_LINK_FAILED;
    }
    public static final class AccountLinkPreview {
        private final String requestId, confirmToken, accountHint, accountIdHint, minecraftName;
        private final long expiresAt, deadlineNanos;
        private AccountLinkPreview(String requestId, String confirmToken, String accountHint, String accountIdHint, String minecraftName, long expiresAt, long deadlineNanos) {
            this.requestId = requestId; this.confirmToken = confirmToken; this.accountHint = accountHint; this.accountIdHint = accountIdHint; this.minecraftName = minecraftName; this.expiresAt = expiresAt; this.deadlineNanos = deadlineNanos;
        }
        public String getAccountHint() { return accountHint; }
        public String getAccountIdHint() { return accountIdHint; }
        public String getMinecraftName() { return minecraftName; }
        public long getExpiresAt() { return expiresAt; }
        public long getRemainingMillis() {
            long remaining = deadlineNanos - System.nanoTime();
            return remaining <= 0 ? 0 : (remaining + 999999) / 1000000;
        }
        private boolean isExpired() { return System.nanoTime() - deadlineNanos >= 0; }
    }
    private static final class AccountLinkPending {
        final String id; final boolean confirm; final long startedNanos, deadlineNanos;
        AccountLinkPending(String id, boolean confirm, long timeoutMillis) {
            this.id = id; this.confirm = confirm; startedNanos = System.nanoTime();
            deadlineNanos = startedNanos + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        }
    }
    public String getCapeDownloadUrl(String hash) {
        if (hash == null || !hash.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid cape hash");
        return httpOrigin + "/v1/capes/" + hash + ".png";
    }
    public void uploadCape(byte[] normalizedPng, String sha256, UploadCallback callback) {
        if (!authenticated || !gameReady()) { callback.onFailure("Online service is not connected"); return; }
        if (upload != null) { callback.onFailure("A cape upload is already in progress"); return; }
        if (System.currentTimeMillis() < nextUploadAllowed) { callback.onFailure("Please wait before uploading again"); return; }
        if (normalizedPng == null || normalizedPng.length < 24 || normalizedPng.length > 512 * 1024
                || sha256 == null || !sha256.matches("[a-f0-9]{64}") || !sha256.equals(hash(normalizedPng))) {
            callback.onFailure("Invalid cape upload"); return;
        }
        upload = new UploadPending(UUID.randomUUID().toString(), Arrays.copyOf(normalizedPng, normalizedPng.length),
                callback, generation, System.currentTimeMillis() + 15000);
        JsonObject json = message("upload_ticket"); json.addProperty("requestId", upload.id);
        json.addProperty("length", normalizedPng.length); json.addProperty("sha256", sha256); send(json);
    }
    private void postCape(UploadPending pending, String ticket) {
        HttpURLConnection connection = null;
        try {
            if (pending.generation != generation) return;
            connection = (HttpURLConnection) new URL(httpOrigin + "/v1/capes/upload").openConnection();
            connection.setInstanceFollowRedirects(false); connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000); connection.setReadTimeout(15000);
            connection.setRequestProperty("Authorization", "Bearer " + ticket);
            connection.setRequestProperty("Content-Type", "image/png"); connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(pending.bytes.length);
            try (OutputStream output = connection.getOutputStream()) { output.write(pending.bytes); }
            int status = connection.getResponseCode();
            if (status == 429 || status == 503) {
                final long headerRetry = retryAfter(connection.getHeaderField("Retry-After"), status);
                mc.addScheduledTask(() -> {
                    if (pending.generation == generation)
                        nextUploadAllowed = Math.max(nextUploadAllowed, System.currentTimeMillis() + headerRetry * 1000);
                });
            }
            InputStream body = status == 200 ? connection.getInputStream() : connection.getErrorStream();
            JsonObject result;
            try (InputStream input = body) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(); byte[] buffer = new byte[1024]; int length;
                while (input != null && (length = input.read(buffer)) != -1) {
                    if (bytes.size() + length > 8192) throw new IOException("Upload response too large");
                    bytes.write(buffer, 0, length);
                }
                result = new JsonParser().parse(new String(bytes.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
            if (status != 200) {
                final long retrySeconds = result.has("retryAfter") ? Math.min(86400, Math.max(1, result.get("retryAfter").getAsLong())) : 1;
                String reason = string(result, "message");
                mc.addScheduledTask(() -> {
                    if (pending.generation == generation)
                        nextUploadAllowed = Math.max(nextUploadAllowed, System.currentTimeMillis() + retrySeconds * 1000);
                    finishUploadFailure(pending, reason.isEmpty() ? "Upload refused" : reason);
                }); return;
            }
            String canonical = string(result, "hash");
            if (!canonical.matches("[a-f0-9]{64}")) throw new IOException("Invalid upload response");
            mc.addScheduledTask(() -> {
                if (upload != pending || pending.generation != generation) return;
                upload = null; pending.callback.onUploaded(canonical);
            });
        } catch (Exception failure) { mc.addScheduledTask(() -> finishUploadFailure(pending, "Cape upload failed")); }
        finally { if (connection != null) connection.disconnect(); }
    }
    private void finishUploadFailure(UploadPending pending, String reason) {
        if (upload != pending) return;
        upload = null; pending.callback.onFailure(reason);
    }
    private static long retryAfter(String value, int status) {
        long seconds = status == 429 ? 60 : 3;
        if (value != null && value.length() <= 128) {
            try { seconds = Long.parseLong(value.trim()); }
            catch (NumberFormatException date) {
                try { seconds = (java.time.ZonedDateTime.parse(value, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant().toEpochMilli() - System.currentTimeMillis() + 999) / 1000; }
                catch (RuntimeException malformed) { }
            }
        }
        return Math.min(86400, Math.max(1, seconds));
    }
    private static String hash(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data); StringBuilder value = new StringBuilder(64);
            for (byte b : digest) value.append(String.format(Locale.ROOT, "%02x", b & 255)); return value.toString();
        } catch (Exception impossible) { throw new IllegalStateException(impossible); }
    }
    private static final class UploadPending {
        final String id; final byte[] bytes; final UploadCallback callback; final long generation;
        long deadline;
        UploadPending(String id, byte[] bytes, UploadCallback callback, long generation, long deadline) {
            this.id = id; this.bytes = bytes; this.callback = callback; this.generation = generation; this.deadline = deadline;
        }
    }
}
