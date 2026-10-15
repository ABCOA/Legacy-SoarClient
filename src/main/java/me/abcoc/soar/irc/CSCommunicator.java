package me.abcoc.soar.irc;

import java.net.Socket;

public class CSCommunicator extends Thread {
    protected static final byte[] HEADER = {0x00, (byte) 0xFF, 0x02, (byte) 0xFF, 0x00};
    protected static final byte[] END = {0x01, (byte) 0xFF, 0x00, (byte) 0xFF, 0x01};
    protected volatile Socket socket;
    protected volatile ChatClientListener listener;
    protected volatile ChatClientSender sender;
    private volatile long heartbeatTime = System.currentTimeMillis();
    public long getHeartbeatTime() { return heartbeatTime; }
    public void flagHeartbeat() { heartbeatTime = System.currentTimeMillis(); }
}
