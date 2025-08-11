package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatClientSender extends CSCommunicator {
    IRCMod ircMod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());
    private static final Queue<ChatPacket> packetQueue = new ConcurrentLinkedDeque<>();
    @Override
    public void run() {
        try {
            while(true) {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                }
                while(!packetQueue.isEmpty()) {
                    ChatPacket packet = packetQueue.poll();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    buffer.write(HEADER);
                    buffer.write(packet.getBuffer());
                    buffer.write(END);

                    socket.getOutputStream().write(buffer.toByteArray());

                    if(!packet.packetType.equals("heartbeat")) {
                        SoarLogger.info("Send packet: " + packet.getJson());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(ChatPacket packet) {
        packetQueue.add(packet);
    }

    public void sendMessage(String msg) {
        ChatPacket packet = new ChatPacket();
        packet.message = msg;
        packet.packetType = "chat";
        packet.senderUUID = Soar.getInstance().getAccountManager().getCurrentAccount().getUuid();
        packet.sender = Soar.getInstance().getAccountManager().getCurrentAccount().getName();
        send(packet);
    }

    public void senQueryAllUsersCommand() {
        ChatPacket packet = new ChatPacket();
        packet.packetType = "query_all_users";
        send(packet);
    }

    public void sendHeartbeat() {
        ChatPacket packet = new ChatPacket();
        packet.packetType = "heartbeat";
        send(packet);
    }

    public void sendAFK(boolean isAFK) {
        ChatPacket packet = new ChatPacket();
        packet.packetType = "afk";
        packet.message = String.valueOf(isAFK);
        packet.initSender();
        send(packet);
    }

}
