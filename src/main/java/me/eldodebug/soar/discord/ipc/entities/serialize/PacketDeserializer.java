package me.eldodebug.soar.discord.ipc.entities.serialize;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.eldodebug.soar.discord.ipc.entities.Packet;

public class PacketDeserializer implements JsonDeserializer<Packet> {

    private final Packet.OpCode op;

    public PacketDeserializer(Packet.OpCode op) {
        this.op = op;
    }

    @Override
    public Packet deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        jsonObject.entrySet().removeIf(entry -> entry.getValue().isJsonNull());
        return new Packet(op, jsonObject);
    }
}