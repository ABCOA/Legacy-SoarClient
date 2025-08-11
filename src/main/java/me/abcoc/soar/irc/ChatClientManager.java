package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;

public class ChatClientManager {

    private static ChatClient chatClient;
    private static IRCMod ircMod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());

    public static void refreshChatClient() {
        if (ircMod.isToggled()) {
            chatClient = new ChatClient();
            ChatClient.retryCount = 3;
            SoarLogger.info("Restarting chat client...");
            chatClient.start();
        }
    }

    public static boolean serverAvailable() {
        return ChatClient.isServerAvailable;
    }

    public static synchronized void stopAndClear() {
        if (chatClient != null) {
            try {
                chatClient.stopIRC();
            } catch (Exception e) {
                SoarLogger.error("Error stopping IRC client", e);
            } finally {
                chatClient = null;
            }
        }
    }

    public static synchronized ChatClient getChatClient() {
        if (!ircMod.isToggled()) return null;
        if (chatClient == null || !chatClient.isAlive()) {
            refreshChatClient();
        }
        return chatClient;
    }
}
