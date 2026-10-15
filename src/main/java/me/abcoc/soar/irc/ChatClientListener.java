package me.abcoc.soar.irc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClientListener extends CSCommunicator {
    private final ChatClient owner;
    private final Consumer<ChatPacket> handler;
    public ChatClientListener(ChatClient owner, Socket socket, Consumer<ChatPacket> handler) {
        this.owner = owner;
        this.socket = socket;
        this.handler = handler;
        setName("IRC Listener");
        setDaemon(true);
    }
    @Override public void run() {
        try {
            InputStream input = new BufferedInputStream(socket.getInputStream(), 8192);
            ChatPacket packet;
            while (!isInterrupted() && (packet = IrcFraming.read(input)) != null) {
                owner.received(packet);
                if (!"heartbeat".equals(packet.packetType)) handler.accept(packet);
            }
            if (!isInterrupted()) owner.peerClosed();
        } catch (IOException | RuntimeException e) {
            owner.connectionFailed(e);
        } finally {
            owner.closeSocket();
        }
    }
}
