package me.abcoc.soar.irc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import java.util.Map;

public final class IrcMessages {
    private IrcMessages() {}
    public static IChatComponent item(ChatPacket packet) {
        IChatComponent text = new ChatComponentText("§a[SoarChat] " + packet.getRank(true) + "§d" + packet.sender + " §f展示了 ");
        try {
            JsonObject json = new JsonParser().parse(packet.message).getAsJsonObject();
            String nbt = ChatPacket.string(json, "nbt"), name = ChatPacket.string(json, "name");
            if (!nbt.isEmpty() && nbt.length() < 65536) {
                text.appendSibling(new ChatComponentText("§b[" + (name.isEmpty() ? "物品" : name) + "]").setChatStyle(new ChatStyle()
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ChatComponentText(nbt)))));
                return text;
            }
        } catch (RuntimeException ignored) {}
        return text.appendText(packet.message.length() > 512 ? packet.message.substring(0, 512) + "…" : packet.message);
    }
    public static String stats(String payload) {
        JsonObject results = new JsonParser().parse(payload).getAsJsonObject();
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, JsonElement> entry : results.entrySet()) {
            text.append("§b").append(entry.getKey()).append("§f: ");
            if (!entry.getValue().isJsonObject()) { text.append("暂无数据\n"); continue; }
            JsonObject result = entry.getValue().getAsJsonObject();
            if (!result.has("player") || !result.get("player").isJsonObject()) {
                String cause = ChatPacket.string(result, "cause");
                text.append(cause.isEmpty() ? "没有可用战绩" : cause).append('\n');
                continue;
            }
            JsonObject player = result.getAsJsonObject("player");
            text.append("经验 ").append(number(player, "networkExp"));
            if (player.has("stats") && player.get("stats").isJsonObject()) {
                JsonObject all = player.getAsJsonObject("stats");
                appendStats(text, all, "Bedwars", "起床", "wins_bedwars", "losses_bedwars", "final_kills_bedwars", "final_deaths_bedwars");
                appendStats(text, all, "SkyWars", "空岛", "wins", "losses", "kills", "deaths");
                appendStats(text, all, "Duels", "决斗", "wins", "losses", "kills", "deaths");
            }
            text.append('\n');
        }
        return text.toString().trim();
    }
    private static long number(JsonObject json, String key) {
        try { return json.has(key) ? json.get(key).getAsLong() : 0; }
        catch (RuntimeException ignored) { return 0; }
    }
    private static void appendStats(StringBuilder text, JsonObject all, String key, String title, String wins, String losses, String kills, String deaths) {
        if (!all.has(key) || !all.get(key).isJsonObject()) return;
        JsonObject mode = all.getAsJsonObject(key);
        long deathCount = number(mode, deaths), lossCount = number(mode, losses);
        text.append("\n§e").append(title).append("§f 胜 ").append(number(mode, wins)).append(" / 负 ").append(lossCount)
                .append("，").append("Bedwars".equals(key) ? "FKDR " : "K/D ")
                .append(String.format(java.util.Locale.ROOT, "%.2f", (double) number(mode, kills) / Math.max(1, deathCount)));
    }
}
