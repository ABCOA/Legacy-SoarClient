package me.eldodebug.soar.management.command.impl;

import me.abcoc.soar.irc.ChatClient;
import me.abcoc.soar.irc.ChatClientManager;
import me.abcoc.soar.irc.ChatClientSender;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;
import me.eldodebug.soar.utils.Multithreading;
import net.minecraft.util.ChatComponentText;

public class IRCCommand extends Command {

    private ChatClient chatClient;
    IRCMod ircMod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());


    public IRCCommand() {
        super("irc");
    }

    @Override
    public void onCommand(String message) {
        String text = message;

        Multithreading.runAsync(() -> {
            if (ircMod.isToggled()) {
                try {
                    chatClient = ChatClientManager.getChatClient();
                    ChatClientSender sender = chatClient.getSender();
                    if (sender != null) {
                        sender.sendMessage(text);
                    } else {
                        SoarLogger.error("Sender is not initialized.");
                    }
                } catch (Exception e) {
                    SoarLogger.error("Failed to send message to IRC.", e);
                }
            } else {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] 未开启IRC功能，请在功能菜单.其他中打开!"));
            }
        });
    }
}