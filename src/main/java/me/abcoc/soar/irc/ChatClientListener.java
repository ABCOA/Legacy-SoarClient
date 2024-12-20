package me.abcoc.soar.irc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.eldodebug.soar.logger.SoarLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;

public class ChatClientListener extends CSCommunicator {
    public static final ClientReceiveHandler handler = new ClientReceiveHandler();
    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            int i;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            boolean startRecordFlag = false;

            while((i = is.read()) != -1) {

                try {
                    buffer.write(i);
                    byte[] bytes = buffer.toByteArray();
                    for(int j = 0; j < Math.min(4, bytes.length); j++) {
                        if((startRecordFlag ? END : HEADER)[j] != bytes[j]) {
                            data.write(bytes);
                            buffer.reset();
                            break;
                        }
                    }
                    if(bytes.length == (startRecordFlag ? END : HEADER).length) {
                        if(startRecordFlag) {
                            try {
                                ChatPacket packet = new ChatPacket();
                                String s = new String(data.toByteArray(), StandardCharsets.UTF_8);
                                s = s.substring(s.indexOf('{'));
                                JsonObject jo = new JsonParser().parse(s).getAsJsonObject();
                                packet.raw = jo;
                                packet.setFromJson(jo);
                                if(!packet.packetType.equals("heartbeat")) {
                                    SoarLogger.info("Received packet: " + packet.getJson());
                                }
                                handler.handle(packet, this);
                            } catch (Exception e) {
                                buffer.reset();
                                data.reset();
                                continue;
                            }
                        } else {
                        }
                        startRecordFlag = !startRecordFlag;
                        buffer.reset();
                        data.reset();
                    }
                } catch (BufferOverflowException e) {
                    buffer.reset();
                    data.reset();
                    startRecordFlag = false;
                }

            }
            SoarLogger.getLogger().info("关闭连接");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
