package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ChatClientManager {
    private static volatile ChatClient chatClient;
    private static IrcConfig config;
    private static volatile boolean leavingGame, shuttingDown;
    private static volatile NetworkManager gameConnection;
    private static Function<Consumer<ChatPacket>, ChatClient> clientFactory = ChatClientManager::createClient;
    private ChatClientManager() {}
    public static boolean isEnabled() {
        if (Soar.getInstance().getModManager() == null) return false;
        IRCMod mod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());
        return mod != null && mod.isToggled();
    }
    public static synchronized IrcConfig getConfig() {
        if (config == null) config = IrcConfig.load();
        return config;
    }
    public static boolean isInWorld() {
        return !leavingGame && !shuttingDown && worldConnection() != null;
    }
    private static NetworkManager worldConnection() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null || mc.thePlayer.worldObj != mc.theWorld) return null;
        NetHandlerPlayClient handler = mc.getNetHandler();
        NetworkManager connection = handler == null ? null : handler.getNetworkManager();
        return connection != null && connection.isChannelOpen() ? connection : null;
    }
    public static boolean isGameSessionActive() {
        NetworkManager connection = gameConnection;
        if (leavingGame || shuttingDown || connection == null || !connection.isChannelOpen()) return false;
        NetworkManager current = worldConnection();
        return current == null || current == connection;
    }
    private static boolean canConnect() { return isEnabled() && isGameSessionActive(); }
    private static ChatClient createClient(Consumer<ChatPacket> handler) {
        Minecraft mc = Minecraft.getMinecraft();
        return new ChatClient(IrcConfig.HOST, IrcConfig.PORT, mc.getSession().getUsername(),
                mc.getSession().getPlayerID(), "Soar " + Soar.getInstance().getVersion(), handler);
    }
    public static synchronized void updateConnectionState() {
        reconcileGameConnection();
        if (!canConnect()) {
            if (chatClient != null) stopAndClear();
            return;
        }
        if (isInWorld() && (chatClient == null || !chatClient.isAlive())) startChatClient();
    }
    private static void reconcileGameConnection() {
        if (leavingGame || shuttingDown || !isEnabled()) {
            gameConnection = null;
            IrcAfk.reset();
            return;
        }
        if (gameConnection != null && !gameConnection.isChannelOpen()) {
            IrcAfk.reset();
            stopAndClear();
            gameConnection = null;
        }
        NetworkManager current = worldConnection();
        if (current != null && current != gameConnection) {
            IrcAfk.reset();
            stopAndClear();
            gameConnection = current;
        }
    }
    public static synchronized void leaveGame() {
        IrcAfk.reset();
        leavingGame = true;
        gameConnection = null;
        stopAndClear();
    }
    public static synchronized void worldChanging() {
        IrcFeatures.worldChanged();
        if (gameConnection != null && !gameConnection.isChannelOpen()) {
            IrcAfk.reset();
            gameConnection = null;
            stopAndClear();
        }
    }
    public static synchronized void enterWorld() {
        leavingGame = false;
        updateConnectionState();
        IrcAfk.onWorldLoaded();
    }
    public static synchronized void shutdown() {
        shuttingDown = true;
        leaveGame();
    }
    public static synchronized void refreshChatClient() {
        stopAndClear();
        reconcileGameConnection();
        if (!canConnect() || !isInWorld()) return;
        startChatClient();
    }
    private static void startChatClient() {
        Minecraft mc = Minecraft.getMinecraft();
        getConfig();
        final ChatClient[] connection = new ChatClient[1];
        connection[0] = clientFactory.apply(packet -> {
                    long epoch = connection[0].getConnectionId();
                    mc.addScheduledTask(() -> {
                        if (chatClient == connection[0] && connection[0].getConnectionId() == epoch && canConnect()) ClientReceiveHandler.handle(packet);
                    });
                });
        chatClient = connection[0];
        chatClient.start();
    }
    public static boolean serverAvailable() {
        ChatClient current = chatClient;
        return canConnect() && current != null && current.isAvailable();
    }
    public static synchronized void stopAndClear() {
        ChatClient previous = chatClient;
        chatClient = null;
        if (previous != null) previous.stopIRC();
        IrcFeatures.clearSession();
    }
    public static synchronized ChatClient getChatClient() {
        updateConnectionState();
        return chatClient;
    }
    public static boolean send(String type, String payload) {
        ChatClient client = getChatClient();
        ChatClientSender sender = client == null ? null : client.getSender();
        if (sender == null) return false;
        sender.sendPayload(type, payload);
        return true;
    }

    public static synchronized boolean sendFor(ChatClient expected, String type, String payload) {
        if (chatClient != expected || !canConnect()) return false;
        ChatClientSender sender = expected == null ? null : expected.getSender();
        if (sender == null) return false;
        sender.sendPayload(type, payload);
        return true;
    }
}
