package me.eldodebug.soar.discord.ipc.entities.pipe;

import com.google.gson.JsonObject;

import me.eldodebug.soar.discord.ipc.IPCClient;
import me.eldodebug.soar.discord.ipc.IPCListener;
import me.eldodebug.soar.discord.ipc.entities.Callback;
import me.eldodebug.soar.discord.ipc.entities.DiscordBuild;
import me.eldodebug.soar.discord.ipc.entities.Packet;
import me.eldodebug.soar.discord.ipc.exceptions.NoDiscordClientException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public abstract class Pipe {

    private static final Logger LOGGER = LogManager.getLogger(Pipe.class);
    private static final int VERSION = 1;
    PipeStatus status = PipeStatus.CONNECTING;
    IPCListener listener;
    private DiscordBuild build;
    final IPCClient ipcClient;
    private final HashMap<String,Callback> callbacks;

    Pipe(IPCClient ipcClient, HashMap<String, Callback> callbacks) {
        this.ipcClient = ipcClient;
        this.callbacks = callbacks;
    }

    public static Pipe openPipe(IPCClient ipcClient, long clientId, HashMap<String,Callback> callbacks,
                                DiscordBuild... preferredOrder) throws NoDiscordClientException {

        if(preferredOrder == null || preferredOrder.length == 0) {
            preferredOrder = new DiscordBuild[]{DiscordBuild.ANY};
        }

        Pipe pipe = null;
        Pipe[] open = new Pipe[DiscordBuild.values().length];
        
        for(int i = 0; i < 10; i++) {
            try {
                String location = getPipeLocation(i);
                LOGGER.debug(String.format("Searching for IPC: %s", location));
                pipe = createPipe(ipcClient, callbacks, location);

                JsonObject handshakeJson = new JsonObject();
                handshakeJson.addProperty("v", VERSION);
                handshakeJson.addProperty("client_id", Long.toString(clientId));
                pipe.send(Packet.OpCode.HANDSHAKE, handshakeJson, null);

                Packet p = pipe.read();

                JsonObject json = p.getJson();
                String apiEndpoint = DiscordBuild.ANY.name();

                if (json.has("data")) {
                    JsonObject data = json.getAsJsonObject("data");
                    if (data.has("config")) {
                        JsonObject config = data.getAsJsonObject("config");
                        if (config.has("api_endpoint")) {
                            apiEndpoint = config.get("api_endpoint").getAsString();
                        }
                    }
                }

                pipe.build = DiscordBuild.from(apiEndpoint);

                LOGGER.debug(String.format("Found a valid client (%s) with packet: %s", pipe.build.name(), p.toString()));
                
                if(pipe.build == preferredOrder[0] || DiscordBuild.ANY == preferredOrder[0]) {
                    LOGGER.info(String.format("Found preferred client: %s", pipe.build.name()));
                    break;
                }

                open[pipe.build.ordinal()] = pipe;
                open[DiscordBuild.ANY.ordinal()] = pipe;

                pipe.build = null;
                pipe = null;
            }
            catch(Exception ex) {
                pipe = null;
            }
        }

        if(pipe == null) {
            for(int i = 1; i < preferredOrder.length; i++) {
                DiscordBuild cb = preferredOrder[i];
                LOGGER.debug(String.format("Looking for client build: %s", cb.name()));
                if(open[cb.ordinal()] != null) {
                    pipe = open[cb.ordinal()];
                    open[cb.ordinal()] = null;
                    if(cb == DiscordBuild.ANY) {
                        for(int k = 0; k < open.length; k++) {
                            if(open[k] == pipe) {
                                pipe.build = DiscordBuild.values()[k];
                                open[k] = null;
                            }
                        }
                    } else {
                    	pipe.build = cb;
                    }

                    LOGGER.info(String.format("Found preferred client: %s", pipe.build.name()));
                    break;
                }
            }
            if(pipe == null) {
                throw new NoDiscordClientException();
            }
        }
        
        for(int i = 0; i < open.length; i++) {
        	
            if(i == DiscordBuild.ANY.ordinal()) {
                continue;
            }
            
            if(open[i] != null) {
                try {
                    open[i].close();
                } catch(Exception ex) {
                    LOGGER.debug("Failed to close an open IPC pipe!", ex);
                }
            }
        }

        pipe.status = PipeStatus.CONNECTED;

        return pipe;
    }

    private static Pipe createPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) {
    	
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return new WindowsPipe(ipcClient, callbacks, location);
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
    }

    public void send(Packet.OpCode op, JsonObject data, Callback callback) {
        try {
            String nonce = generateNonce();
            data.addProperty("nonce", nonce);
            Packet p = new Packet(op, data);
            if(callback!=null && !callback.isEmpty())
                callbacks.put(nonce, callback);
            write(p.toBytes());
            LOGGER.debug(String.format("Sent packet: %s", p.toString()));
            if(listener != null)
                listener.onPacketSent(ipcClient, p);
        } catch(IOException ex) {
            LOGGER.error("Encountered an IOException while sending a packet and disconnected!");
            status = PipeStatus.DISCONNECTED;
        }
    }

    public abstract Packet read() throws IOException;

    public abstract void write(byte[] b) throws IOException;

    private static String generateNonce() {
        return UUID.randomUUID().toString();
    }

    public PipeStatus getStatus() {
        return status;
    }

    public void setStatus(PipeStatus status) {
        this.status = status;
    }

    public void setListener(IPCListener listener) {
        this.listener = listener;
    }

    public abstract void close() throws IOException;

    public DiscordBuild getDiscordBuild() {
        return build;
    }

    private final static String[] unixPaths = {"XDG_RUNTIME_DIR","TMPDIR","TMP","TEMP"};

    private static String getPipeLocation(int i) {
    	
        if(System.getProperty("os.name").contains("Win")) {
            return "\\\\?\\pipe\\discord-ipc-"+i;
        }

        String tmppath = null;
        
        for(String str : unixPaths) {
        	
            tmppath = System.getenv(str);
            
            if(tmppath != null) {
                break;
            }
        }
        
        if(tmppath == null) {
            tmppath = "/tmp";
        }
        
        return tmppath+"/discord-ipc-"+i;
    }
}
