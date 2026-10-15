package me.abcoc.soar.irc;

import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class IrcFraming {
    public static final int MAX_PACKET_BYTES = 24 * 1024 * 1024;
    private IrcFraming() {}
    public static void write(OutputStream output, ChatPacket packet) throws IOException {
        byte[] bytes = packet.getBuffer();
        if (bytes.length > MAX_PACKET_BYTES) throw new IOException("IRC packet exceeds size limit");
        output.write(CSCommunicator.HEADER);
        output.write(bytes);
        output.write(CSCommunicator.END);
        output.flush();
    }
    public static ChatPacket read(InputStream input) throws IOException {
        int matched = 0, next;
        while ((next = input.read()) != -1) {
            if (next == (CSCommunicator.HEADER[matched] & 255)) {
                if (++matched == CSCommunicator.HEADER.length) break;
            } else matched = next == (CSCommunicator.HEADER[0] & 255) ? 1 : 0;
        }
        if (next == -1) return null;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        matched = 0;
        while ((next = input.read()) != -1) {
            if (next == (CSCommunicator.END[matched] & 255)) {
                if (++matched == CSCommunicator.END.length) {
                    try {
                        ChatPacket packet = new ChatPacket();
                        packet.setFromJson(new JsonParser().parse(new String(data.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject());
                        if (packet.packetType.isEmpty()) throw new IllegalArgumentException("Missing packetType");
                        return packet;
                    } catch (RuntimeException e) { throw new IOException("Malformed IRC JSON", e); }
                }
            } else {
                if (matched > 0) data.write(CSCommunicator.END, 0, matched);
                matched = 0;
                if (next == (CSCommunicator.END[0] & 255)) matched = 1;
                else data.write(next);
            }
            if (data.size() > MAX_PACKET_BYTES) throw new IOException("IRC packet exceeds size limit");
        }
        throw new IOException("Incomplete IRC frame");
    }
}
