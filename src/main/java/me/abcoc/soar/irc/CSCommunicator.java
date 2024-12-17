package me.abcoc.soar.irc;

import java.net.Socket;

public class CSCommunicator extends Thread{
    protected static final byte[] HEADER = new byte[] {0x00, (byte) 0xFF, 0x02, (byte) 0xFF, 0x00};
    protected static final byte[] END = new byte[] {0x01, (byte) 0xFF, 0x00, (byte) 0xFF, 0x01};
    public static Socket socket = null;
    public static ChatClientListener listener = null;
    public static ChatClientSender sender = null;
    private static long heartbeatTime = System.currentTimeMillis();

    public void flagHeartbeat() {
        heartbeatTime = System.currentTimeMillis();
    }
}
