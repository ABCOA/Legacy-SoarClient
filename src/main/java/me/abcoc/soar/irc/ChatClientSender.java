package me.abcoc.soar.irc;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClientSender extends CSCommunicator {
    private final ChatClient owner;
    private final LinkedBlockingQueue<ChatPacket> packets = new LinkedBlockingQueue<ChatPacket>(128);
    public ChatClientSender(ChatClient owner, Socket socket) {
        this.owner = owner;
        this.socket = socket;
        setName("IRC Sender");
        setDaemon(true);
    }
    @Override public void run() {
        try {
            while (!isInterrupted() && !socket.isClosed()) IrcFraming.write(socket.getOutputStream(), packets.take());
        } catch (InterruptedException e) {
            interrupt();
        } catch (IOException e) {
            owner.connectionFailed(e);
        }
    }
    public void send(ChatPacket packet) {
        if (isInterrupted() || socket.isClosed()) throw new IllegalStateException("IRC is disconnected");
        packet.sender = owner.getPlayerName();
        packet.senderUUID = owner.getPlayerUuid();
        if (!packets.offer(packet)) throw new IllegalStateException("IRC send queue is full");
    }
    public void sendMessage(String message) { send(new ChatPacket("chat", message)); }
    public void senQueryAllUsersCommand() { send(new ChatPacket("query_all_users", "")); }
    public void sendHeartbeat() { send(new ChatPacket("heartbeat", "")); }
    public void sendAFK(boolean afk) { send(new ChatPacket("afk", Boolean.toString(afk))); }
    public void sendPayload(String type, String message) { send(new ChatPacket(type, message)); }
}
