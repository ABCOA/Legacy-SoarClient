package me.abcoc.soar.irc;

import com.google.gson.JsonObject;
import me.eldodebug.soar.management.online.LocationCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class IrcFeatures {
    private static volatile String location = "", sentLocation = "";
    private static volatile boolean wasConnected;
    private static final Map<String, Integer> party = new java.util.concurrent.ConcurrentHashMap<String, Integer>();
    private IrcFeatures() {}

    public static void clearSession() {
        location = "";
        sentLocation = "";
        wasConnected = false;
        party.clear();
        IrcUsers.clear();
    }

    public static void print(String text) { print(new ChatComponentText("§a[SoarChat] §r" + text)); }
    public static void print(IChatComponent component) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.ingameGUI != null) mc.ingameGUI.getChatGUI().printChatMessage(component);
        });
    }

    public static boolean send(String type, String payload) {
        if (!ChatClientManager.isEnabled()) { print("§c请先在功能菜单中开启 IRC。"); return false; }
        ChatClientManager.updateConnectionState();
        if (!ChatClientManager.isGameSessionActive()) { print("§e进入世界或连接游戏服务器后，IRC 才会自动连接。"); return false; }
        try {
            if (!ChatClientManager.send(type, payload)) {
                ChatClient client = ChatClientManager.getChatClient();
                String error = client == null ? "" : client.getLastError();
                print(error.isEmpty() ? "§eIRC 正在连接或等待登录确认，请稍后重试。" : "§cIRC 连接不可用: " + error);
                return false;
            }
            return true;
        } catch (RuntimeException e) { print("§c发送失败: " + e.getMessage()); return false; }
    }

    public static void tick() {
        if (!ChatClientManager.serverAvailable()) { wasConnected = false; return; }
        if (!ChatClientManager.isInWorld()) return;
        IrcConfig config = ChatClientManager.getConfig();
        if (!wasConnected) {
            wasConnected = true;
            sentLocation = "";
        }
        if (!config.shareLocation) return;
        receiveGameChat(null);
        if (!location.isEmpty() && !location.equals(sentLocation) && ChatClientManager.send("onlineinfo", location)) {
            sentLocation = location;
        }
    }

    public static void receiveGameChat(String message) {
        if (!ChatClientManager.getConfig().shareLocation) return;
        LocationCollector.Location current = LocationCollector.getInstance().getLocation();
        if (!"hypixel".equals(current.getNetwork())) {
            location = IrcPayloads.location("null", "null", "null", "null"); return;
        }
        if (!current.isKnown()) { location = ""; sentLocation = ""; return; }
        String game = "SKYBLOCK".equalsIgnoreCase(current.getGame()) ? "SkyBlock" : current.getGame();
        location = IrcPayloads.location(current.getServer(), ircField(game), ircField(current.getLobby()), ircField(current.getMode()));
    }

    private static String ircField(String value) { return value == null || value.isEmpty() ? "null" : value; }

    public static void setLocation(String server, String game, String lobby, String mode) {
        location = IrcPayloads.location(server, game, lobby, mode);
        if (ChatClientManager.getConfig().shareLocation && !location.equals(sentLocation)) {
            if (send("onlineinfo", location)) {
                sentLocation = location;
            }
        }
    }

    public static void showItem() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack item = mc.thePlayer == null ? null : mc.thePlayer.getHeldItem();
        if (item == null) { print("§e请先手持要展示的物品。"); return; }
        JsonObject json = new JsonObject();
        json.addProperty("name", item.getDisplayName());
        json.addProperty("count", item.stackSize);
        json.addProperty("nbt", item.writeToNBT(new NBTTagCompound()).toString());
        send("showitem", json.toString());
    }

    public static void partyMember(String uuid, int role) {
        uuid = uuid.replace("-", "");
        if (!uuid.matches("[a-fA-F0-9]{32}") || role < 0 || role > 2) throw new IllegalArgumentException("UUID 或队伍角色无效");
        party.put(uuid, role);
        publishParty();
    }
    public static void leaveParty() { party.clear(); publishParty(); }
    public static void publishParty() {
        if (!ChatClientManager.getConfig().shareParty) return;
        Map<String, Integer> snapshot = new HashMap<String, Integer>(party);
        String[] uuids = snapshot.keySet().toArray(new String[0]);
        int[] roles = new int[uuids.length];
        for (int i = 0; i < uuids.length; i++) roles[i] = snapshot.get(uuids[i]);
        send("hyp_party", IrcPayloads.party(!snapshot.isEmpty(), uuids, roles));
    }

    public static void worldChanged() { location = ""; sentLocation = ""; }
    public static Map<String, Integer> getParty() { return Collections.unmodifiableMap(party); }

}
