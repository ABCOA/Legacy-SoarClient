package me.eldodebug.soar.discord.ipc.entities;

import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {
	
    private final OpCode op;
    private final JsonObject data;

    public Packet(OpCode op, JsonObject data) {
        this.op = op;
        this.data = data;
    }

    public byte[] toBytes() {
        byte[] d = data.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer packet = ByteBuffer.allocate(d.length + 2*Integer.BYTES);
        packet.putInt(Integer.reverseBytes(op.ordinal()));
        packet.putInt(Integer.reverseBytes(d.length));
        packet.put(d);
        return packet.array();
    }
    
    public OpCode getOp() {
        return op;
    }
    
    public JsonObject getJson() {
        return data;
    }
    
    @Override
    public String toString() {
        return "Pkt:"+getOp()+getJson().toString();
    }
    
    public enum OpCode {
        HANDSHAKE, FRAME, CLOSE, PING, PONG
    }
}
