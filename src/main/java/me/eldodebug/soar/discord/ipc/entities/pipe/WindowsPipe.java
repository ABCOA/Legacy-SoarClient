package me.eldodebug.soar.discord.ipc.entities.pipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import me.eldodebug.soar.discord.ipc.IPCClient;
import me.eldodebug.soar.discord.ipc.entities.Callback;
import me.eldodebug.soar.discord.ipc.entities.Packet;
import me.eldodebug.soar.discord.ipc.entities.serialize.PacketDeserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class WindowsPipe extends Pipe {

    private static final Logger LOGGER = LogManager.getLogger(WindowsPipe.class);

    private final RandomAccessFile file;

    WindowsPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) {
        super(ipcClient, callbacks);
        try {
            this.file = new RandomAccessFile(location, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
    }

    @Override
    public Packet read() throws IOException {
    	
        while(file.length() == 0 && status == PipeStatus.CONNECTED) {
            try {
                Thread.sleep(50);
            } catch(InterruptedException ignored) {}
        }

        if(status==PipeStatus.DISCONNECTED) {
            throw new IOException("Disconnected!");
        }

        if(status==PipeStatus.CLOSED) {
            return new Packet(Packet.OpCode.CLOSE, null);
        }

        Packet.OpCode op = Packet.OpCode.values()[Integer.reverseBytes(file.readInt())];
        int len = Integer.reverseBytes(file.readInt());
        byte[] d = new byte[len];

        file.readFully(d);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Packet.class, new PacketDeserializer(op))
                .create();
        JsonObject jsonObject = gson.fromJson(new String(d), JsonObject.class);
        Packet p = gson.fromJson(jsonObject, Packet.class);

        LOGGER.debug(String.format("Received packet: %s", p.toString()));
        
        if(listener != null) {
            listener.onPacketReceived(ipcClient, p);
        }
        
        return p;
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("Closing IPC pipe...");
        send(Packet.OpCode.CLOSE, new JsonObject(), null);
        status = PipeStatus.CLOSED;
        file.close();
    }
}
