package me.abcoc.soar.irc;

import me.eldodebug.soar.logger.SoarLogger;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient extends CSCommunicator {
    private final String host, playerName, playerUuid, version;
    private final int port;
    private final Consumer<ChatPacket> handler;
    private volatile boolean stopped, available, failureReported;
    private volatile String lastError = "";
    private volatile String serverMessage = "";
    private volatile long connectionId;
    public ChatClient(String host, int port, String name, String uuid, String version, Consumer<ChatPacket> handler) {
        this.host = host;
        this.port = port;
        this.playerName = name;
        this.playerUuid = uuid.replace("-", "");
        this.version = version;
        this.handler = handler;
        setName("Soar IRC Connection");
        setDaemon(true);
    }
    @Override public void run() {
        int failures = 0;
        while (!stopped && !isInterrupted()) {
            try {
                failureReported = false;
                serverMessage = "";
                available = false;
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 10000);
                if (stopped || isInterrupted()) break;
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(125000);
                connectionId++;
                flagHeartbeat();
                sender = new ChatClientSender(this, socket);
                listener = new ChatClientListener(this, socket, handler);
                sender.send(new ChatPacket("join", version));
                SoarLogger.info("TCP connected to " + host + ":" + port + "; waiting for IRC login confirmation");
                sender.start();
                listener.start();
                long nextHeartbeat = 0, connectedAt = System.currentTimeMillis();
                while (!stopped && sender.isAlive() && listener.isAlive()) {
                    long now = System.currentTimeMillis();
                    if (now - getHeartbeatTime() > 120000) throw new IOException("IRC heartbeat timeout");
                    if (now >= nextHeartbeat) {
                        sender.sendHeartbeat();
                        nextHeartbeat = now + 30000;
                    }
                    if (now - connectedAt > 10000) failures = 0;
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                interrupt();
            } catch (IOException | RuntimeException e) {
                connectionFailed(e);
            } finally {
                available = false;
                closeSocket();
                if (sender != null) sender.interrupt();
                if (listener != null) listener.interrupt();
                joinWorker(sender);
                joinWorker(listener);
                sender = null;
                listener = null;
            }
            if (!stopped && !isInterrupted()) {
                long retryDelay = Math.min(30000, 5000L * ++failures);
                SoarLogger.info("IRC reconnect scheduled in " + retryDelay + " ms");
                try { Thread.sleep(retryDelay); }
                catch (InterruptedException e) { interrupt(); }
            }
        }
    }
    private void joinWorker(Thread worker) {
        if (worker == null) return;
        try { worker.join(1000); }
        catch (InterruptedException e) { interrupt(); }
    }
    synchronized void received(ChatPacket packet) {
        if (stopped || failureReported) return;
        if ("system".equals(packet.packetType)) {
            String text = packet.message.replaceAll("(?i)§[0-9A-FK-OR]", "");
            serverMessage = text.length() > 240 ? text.substring(0, 240) : text;
        }
        boolean heartbeat = "heartbeat".equals(packet.packetType);
        if (heartbeat) flagHeartbeat();
        boolean ownJoin = "join".equals(packet.packetType) && playerName.equals(packet.sender)
                && playerUuid.equals(packet.senderUUID.replace("-", ""));
        if (!available && (heartbeat || ownJoin)) {
            available = true;
            lastError = "";
            SoarLogger.info("IRC login confirmed at " + host + ":" + port);
        }
    }
    synchronized void peerClosed() {
        String reason = available ? "IRC 对端关闭了连接。" : "IRC 对端在登录确认前关闭连接，请检查 TCP 转发或 CDN 代理。";
        if (!serverMessage.isEmpty()) reason += " 服务端消息: " + serverMessage;
        connectionFailed(new EOFException(reason));
    }
    synchronized void connectionFailed(Exception e) {
        if (stopped || failureReported) return;
        failureReported = true;
        lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        SoarLogger.warn("IRC connection failed: " + lastError);
        closeSocket();
    }
    void closeSocket() {
        available = false;
        Socket current = socket;
        if (current != null) try { current.close(); } catch (IOException ignored) {}
    }
    public void stopIRC() {
        stopped = true;
        interrupt();
        closeSocket();
        if (sender != null) sender.interrupt();
        if (listener != null) listener.interrupt();
    }
    public boolean isAvailable() {
        ChatClientSender current = sender;
        return available && !stopped && current != null && current.isAlive();
    }
    public ChatClientSender getSender() { return isAvailable() ? sender : null; }
    public String getPlayerName() { return playerName; }
    public String getPlayerUuid() { return playerUuid; }
    public String getLastError() { return lastError; }
    public long getConnectionId() { return connectionId; }
}
