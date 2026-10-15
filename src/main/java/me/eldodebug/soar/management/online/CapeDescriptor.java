package me.eldodebug.soar.management.online;

import com.google.gson.JsonObject;
import java.util.Objects;

public final class CapeDescriptor {
    private final String type, id, hash;
    private CapeDescriptor(String type, String id, String hash) {
        this.type = type; this.id = id; this.hash = hash;
    }
    public static CapeDescriptor official() { return new CapeDescriptor("official", "", ""); }
    public static CapeDescriptor none() { return new CapeDescriptor("none", "", ""); }
    public static CapeDescriptor builtin(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_./-]{1,128}")) throw new IllegalArgumentException("Invalid cape id");
        return new CapeDescriptor("builtin", id, "");
    }
    public static CapeDescriptor custom(String hash) {
        if (hash == null || !hash.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Invalid cape hash");
        return new CapeDescriptor("custom", "", hash);
    }
    public String getType() { return type; }
    public String getKind() { return type; }
    public String getId() { return id; }
    public String getHash() { return hash; }
    public JsonObject toJson() {
        JsonObject json = new JsonObject(); json.addProperty("kind", type);
        if (!id.isEmpty()) json.addProperty("id", id);
        if (!hash.isEmpty()) json.addProperty("hash", hash);
        return json;
    }
    public static CapeDescriptor fromJson(JsonObject json) {
        String kind = json.get("kind").getAsString();
        if ("none".equals(kind)) return none();
        if ("builtin".equals(kind)) return builtin(json.get("id").getAsString());
        if ("custom".equals(kind)) return custom(json.get("hash").getAsString());
        if ("official".equals(kind)) return official();
        throw new IllegalArgumentException("Unknown cape kind");
    }
    @Override public boolean equals(Object other) {
        if (!(other instanceof CapeDescriptor)) return false;
        CapeDescriptor cape = (CapeDescriptor) other;
        return type.equals(cape.type) && id.equals(cape.id) && hash.equals(cape.hash);
    }
    @Override public int hashCode() { return Objects.hash(type, id, hash); }
}
