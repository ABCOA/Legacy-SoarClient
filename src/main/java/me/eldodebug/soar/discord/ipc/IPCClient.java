package me.eldodebug.soar.discord.ipc;

import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import me.eldodebug.soar.discord.ipc.entities.Callback;
import me.eldodebug.soar.discord.ipc.entities.DiscordBuild;
import me.eldodebug.soar.discord.ipc.entities.Packet;
import me.eldodebug.soar.discord.ipc.entities.Packet.OpCode;
import me.eldodebug.soar.discord.ipc.entities.RichPresence;
import me.eldodebug.soar.discord.ipc.entities.User;
import me.eldodebug.soar.discord.ipc.entities.pipe.Pipe;
import me.eldodebug.soar.discord.ipc.entities.pipe.PipeStatus;
import me.eldodebug.soar.discord.ipc.exceptions.NoDiscordClientException;

public final class IPCClient implements Closeable {
	
    private static final Logger LOGGER = LogManager.getLogger(IPCClient.class);
    private final long clientId;
    private final HashMap<String,Callback> callbacks = new HashMap<>();
    private volatile Pipe pipe;
    private IPCListener listener = null;
    private Thread readThread = null;
    
    public IPCClient(long clientId) {
        this.clientId = clientId;
    }
    
    public void setListener(IPCListener listener) {
        this.listener = listener;
        
        if (pipe != null) {
            pipe.setListener(listener);
        }
    }
    
    public void connect(DiscordBuild... preferredOrder) throws NoDiscordClientException {
    	
    	if(isConnected(false)) {
    		return;
    	}
    	
        callbacks.clear();
        pipe = null;

        pipe = Pipe.openPipe(this, clientId, callbacks, preferredOrder);

        LOGGER.debug("Client is now connected and ready!");
        
        if(listener != null) {
            listener.onReady(this);
        }
        
        startReading();
    }
    
    public void sendRichPresence(RichPresence presence) {
        sendRichPresence(presence, null);
    }
    
    public void sendRichPresence(RichPresence presence, Callback callback) {
    	
    	if(isConnected(true)) {
    		return;
    	}
    	
        LOGGER.debug("Sending RichPresence to discord: "+(presence == null ? null : presence.toJson().toString()));

        JsonObject argsObject = new JsonObject();
        argsObject.addProperty("pid", getPID());
        argsObject.add("activity", presence == null ? null : presence.toJson());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cmd", "SET_ACTIVITY");
        jsonObject.add("args", argsObject);

        pipe.send(OpCode.FRAME, jsonObject, callback);
    }

    public void subscribe(Event sub) {
        subscribe(sub, null);
    }
    
    public void subscribe(Event sub, Callback callback) {
    	
    	if(isConnected(true)) {
    		return;
    	}
    	
        if(!sub.isSubscribable()) {
            throw new IllegalStateException("Cannot subscribe to "+sub+" event!");
        }
        
        LOGGER.debug(String.format("Subscribing to Event: %s", sub.name()));
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cmd", "SUBSCRIBE");
        jsonObject.addProperty("evt", sub.name());

        pipe.send(OpCode.FRAME, jsonObject, callback);
    }

    public PipeStatus getStatus() {
    	
        if (pipe == null) {
        	return PipeStatus.UNINITIALIZED;
        }

        return pipe.getStatus();
    }

    @Override
    public void close() {
    	
    	if(isConnected(true)) {
    		return;
    	}
    	
        try {
            pipe.close();
        } catch (IOException e) {
            LOGGER.debug("Failed to close pipe", e);
        }
    }

    public DiscordBuild getDiscordBuild() {
    	
        if (pipe == null) {
        	return null;
        }

        return pipe.getDiscordBuild();
    }

    public enum Event {
        NULL(false),
        READY(false),
        ERROR(false),
        ACTIVITY_JOIN(true),
        ACTIVITY_SPECTATE(true),
        ACTIVITY_JOIN_REQUEST(true),
        UNKNOWN(false);
        
        private final boolean subscribable;
        
        Event(boolean subscribable) {
            this.subscribable = subscribable;
        }
        
        public boolean isSubscribable() {
            return subscribable;
        }
        
        static Event of(String str) {
        	
            if(str==null) {
                return NULL;
            }
            
            for(Event s : Event.values()) {
                if(s != UNKNOWN && s.name().equalsIgnoreCase(str)) {
                    return s;
                }
            }
            
            return UNKNOWN;
        }
    }

    private boolean isConnected(boolean connected) {
    	
        if(connected && getStatus() != PipeStatus.CONNECTED) {
        	return false;
        }

        if(!connected && getStatus() == PipeStatus.CONNECTED) {
        	return true;
        }
        
        return false;
    }
    
    private void startReading() {
    	
        readThread = new Thread(() -> {
            try  {
                Packet p;
                while((p = pipe.read()).getOp() != OpCode.CLOSE) {
                	
                    JsonObject json = p.getJson();

                    Event event = Event.of(json.has("evt") ? json.get("evt").getAsString() : null);
                    String nonce = json.has("nonce") ? json.get("nonce").getAsString() : null;

                    switch(event) {
                        case NULL:
                            if(nonce != null && callbacks.containsKey(nonce))
                                callbacks.remove(nonce).succeed(p);
                            break;

                        case ERROR:
                            if (nonce != null && callbacks.containsKey(nonce)) {
                                JsonObject data = json.getAsJsonObject("data");
                                callbacks.remove(nonce).fail(data.has("message") ? data.get("message").getAsString() : null);
                            }
                            break;
                        case ACTIVITY_JOIN:
                            LOGGER.debug("Reading thread received a 'join' event.");
                            break;

                        case ACTIVITY_SPECTATE:
                            LOGGER.debug("Reading thread received a 'spectate' event.");
                            break;

                        case ACTIVITY_JOIN_REQUEST:
                            LOGGER.debug("Reading thread received a 'join request' event.");
                            break;

                        case UNKNOWN:
                            LOGGER.debug("Reading thread encountered an event with an unknown type: " +
                                         json.get("evt").getAsString());
                            break;
						default:
							break;
                    }
                    
                    if(listener != null && json.has("cmd") && json.get("cmd").getAsString().equals("DISPATCH")) {
                    	
                        try {
                            JsonObject data = json.getAsJsonObject("data");
                            switch(Event.of(json.get("evt").getAsString())) {
                                case ACTIVITY_JOIN:
                                    listener.onActivityJoin(this, data.get("secret").getAsString());
                                    break;

                                case ACTIVITY_SPECTATE:
                                    listener.onActivitySpectate(this, data.get("secret").getAsString());
                                    break;

                                case ACTIVITY_JOIN_REQUEST:
                                    JsonObject u = data.getAsJsonObject("user");
                                    User user = new User(
                                        u.get("username").getAsString(),
                                        u.get("discriminator").getAsString(),
                                        Long.parseLong(u.get("id").getAsString()),
                                        u.has("avatar") ? u.get("avatar").getAsString() : null
                                    );
                                    listener.onActivityJoinRequest(this, data.has("secret") ? data.get("secret").getAsString() : null, user);
                                    break;
								default:
									break;
                            }
                        }
                        catch(Exception e) {
                            LOGGER.error("Exception when handling event: ", e);
                        }
                    }
                }
                
                pipe.setStatus(PipeStatus.DISCONNECTED);
                
                if(listener != null) {
                    listener.onClose(this, p.getJson());
                }
            } catch(IOException ex) {
            	
                LOGGER.error("Reading thread encountered an IOException", ex);

                pipe.setStatus(PipeStatus.DISCONNECTED);
                
                if(listener != null) {
                    listener.onDisconnect(this, ex);
                }
            }
        });

        LOGGER.debug("Starting IPCClient reading thread!");
        readThread.start();
    }
    
    private static int getPID() {
        String pr = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(pr.substring(0,pr.indexOf('@')));
    }
}
