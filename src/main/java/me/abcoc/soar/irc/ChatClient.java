package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class ChatClient extends CSCommunicator {

    public static boolean isServerAvailable = false;
    public static int retryCount = 0;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static IRCMod ircMod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());

    public static int doWhileToken = 0;
    public int currentToken = 0;
    private String ircUrl = "irctest.soarclient.org";
    private int ircPort = 831;

    @Override
    public void run() {
        try {
            long time = System.currentTimeMillis();
            doWhileToken = currentToken = new Random().nextInt();
            SoarLogger.info("IRC client starting with token " + currentToken);
            connect();
            listener = new ChatClientListener();
            sender = new ChatClientSender();
            listener.setName("IRC Listener");
            sender.setName("IRC Sender");
            listener.start();
            sender.start();

            SoarLogger.info("Initialized IRC client in " + (System.currentTimeMillis() - time) / 1000d + "s");

            sendInitPackets();

            isServerAvailable = true;
            flagHeartbeat();
            int heartbeatCounter = 0;
            while (doWhileToken == currentToken && ircMod.isToggled()) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { }
                if ((!listener.isAlive()) || (!sender.isAlive())) {
                    throw new Exception("Sender or listener isn't alive, stopping...");
                }
                if(System.currentTimeMillis() - getHeartbeatTime() > 120000) {
                    throw new IOException("Heartbeat Timeout");
                }
                if(heartbeatCounter % 60 == 0) sender.sendHeartbeat();
                if(heartbeatCounter ++ == 10) retryCount = 5;
            }
        } catch (Exception e) {
            SoarLogger.error("An error occurred in the online chat thread and it is stopped", e);
            isServerAvailable = false;
        } finally {
            isServerAvailable = false;
            if (listener != null) { listener.interrupt(); }
            if (sender != null) { sender.interrupt(); }
            if (socket != null) {
                try { socket.close(); } catch (IOException e) { }
            }
            listener = null;
            sender = null;
            socket = null;
            if (retryCount > 0) {
                retryCount --;
                try { sleep(15000); } catch (InterruptedException e) { }
                ChatClientManager.refreshChatClient();
            }
        }
    }

    public void sendInitPackets() {
        ChatPacket packet = new ChatPacket();
        packet.initSender();
        packet.packetType = "join";
        sender.send(packet);
    }

    public void connect() throws Exception {
        int maxRetries = 3;
        int currentRetry = 0;

        while (currentRetry <= maxRetries) {
            try {
                SoarLogger.info("Connecting to IRC server... (Attempt " + (currentRetry + 1) + ")");
                socket = new Socket(ircUrl, ircPort);
                SoarLogger.info("Successfully connected to IRC server.");
                return;
            } catch (IOException e) {
                SoarLogger.error("Failed to connect to IRC server: " + e.getMessage());
                currentRetry++;
                if (currentRetry <= maxRetries) {
                    SoarLogger.info("Retrying in 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Connection thread was interrupted.", interruptedException);
                    }
                }
            }
        }

        throw new IOException("Failed to connect to IRC server after " + maxRetries + " attempts.");
    }

    public void stopIRC() {
        try {
            SoarLogger.info("Stopping IRC...");
            retryCount = 0;
            isServerAvailable = false;
            doWhileToken = 0;
            stopThreads();
        } catch (Exception e) {
            SoarLogger.error("Failed to stop IRC bot", e);
        }
    }

    private void stopThreads() {
        try {
            if (listener != null) listener.interrupt();
            if (sender != null) sender.interrupt();
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (IOException ignored) {}
            }
            try {
                if (listener != null) listener.join(1000);
                if (sender != null) sender.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            listener = null;
            sender = null;
            socket = null;
        }
    }

    public ChatClientSender getSender() {
        return sender;
    }
}
