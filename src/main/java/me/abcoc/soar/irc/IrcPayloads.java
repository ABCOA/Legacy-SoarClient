package me.abcoc.soar.irc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class IrcPayloads {
    private IrcPayloads() {}
    public static String location(String server, String game, String lobby, String mode) {
        JsonObject json = new JsonObject();
        json.addProperty("serverID", server);
        json.addProperty("gameType", game);
        json.addProperty("lobbyName", lobby);
        json.addProperty("mode", mode);
        return json.toString();
    }
    public static String party(boolean inParty, String[] uuids, int[] roles) {
        if (uuids.length != roles.length) throw new IllegalArgumentException("Party member roles do not match");
        JsonObject json = new JsonObject();
        JsonArray members = new JsonArray();
        for (int i = 0; i < uuids.length; i++) {
            if (!uuids[i].replace("-", "").matches("[a-fA-F0-9]{32}") || roles[i] < 0 || roles[i] > 2)
                throw new IllegalArgumentException("Invalid party member");
            JsonObject member = new JsonObject();
            member.addProperty("uuid", uuids[i].replace("-", ""));
            member.addProperty("role", roles[i]);
            members.add(member);
        }
        json.addProperty("inParty", inParty);
        json.add("members", members);
        return json.toString();
    }
}
