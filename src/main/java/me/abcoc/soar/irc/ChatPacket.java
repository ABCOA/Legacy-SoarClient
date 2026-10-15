package me.abcoc.soar.irc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class ChatPacket {
    private static final Pattern SKYDIAO_RANK = Pattern.compile("\\[(?:YSM)?Skb\\]", Pattern.CASE_INSENSITIVE);
    public JsonObject raw;
    public String message = "", packetType = "", sender = "", senderUUID = "", senderRankName = "";
    public String appendedSenderName = "";
    public ChatPacket() {}
    public ChatPacket(String type, String message) {
        this.packetType = type;
        this.message = message == null ? "" : message;
    }
    public byte[] getBuffer() { return getJson().toString().getBytes(StandardCharsets.UTF_8); }
    public JsonObject getJson() {
        JsonObject json = raw == null ? new JsonObject() : new com.google.gson.JsonParser().parse(raw.toString()).getAsJsonObject();
        json.addProperty("message", message == null ? "" : message);
        json.addProperty("packetType", packetType == null ? "" : packetType);
        json.addProperty("sender", sender == null ? "" : sender);
        json.addProperty("senderUUID", senderUUID == null ? "" : senderUUID);
        json.addProperty("senderRankName", senderRankName == null ? "" : senderRankName);
        return json;
    }
    public void setFromJson(JsonObject json) {
        raw = json;
        message = string(json, "message");
        packetType = string(json, "packetType");
        sender = string(json, "sender");
        senderUUID = string(json, "senderUUID");
        senderRankName = string(json, "senderRankName");
        appendedSenderName = getRank(true) + sender;
    }
    public static String string(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value == null || value.isJsonNull() || !value.isJsonPrimitive() ? "" : value.getAsString();
    }
    public String getRank(boolean space) {
        return senderRankName == null || senderRankName.isEmpty() ? "" : senderRankName + (space ? " " : "");
    }
    public boolean hasSkyDiaoRank() {
        String rank = EnumChatFormatting.getTextWithoutFormattingCodes(senderRankName == null ? "" : senderRankName);
        return SKYDIAO_RANK.matcher(rank).find();
    }
    public void initSender() {
        sender = Minecraft.getMinecraft().getSession().getUsername();
        senderUUID = Minecraft.getMinecraft().getSession().getPlayerID().replace("-", "");
    }
}
