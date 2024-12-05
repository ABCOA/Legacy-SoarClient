package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.account.Account;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.nio.charset.StandardCharsets;

public class MyBot extends ListenerAdapter {
    private static PircBotX bot = null;
    private static Thread botThread = null;
    private static volatile boolean running = true;
    public static Account currentAccount = Soar.getInstance().getAccountManager().getCurrentAccount();
    public static String currentID = currentAccount.getName();
    public static Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        String message;
        try {
            message = new String(event.getMessage().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } catch (Exception e) {
            message = event.getMessage();
        }

        if (event.getUser().getNick().equals("ABCOC")) {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[IRC] " + EnumChatFormatting.DARK_PURPLE + "[管理员]" + EnumChatFormatting.RED + event.getUser().getNick() + EnumChatFormatting.WHITE + ": " + message));
        } else {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[IRC] " + EnumChatFormatting.WHITE + event.getUser().getNick() + ": " + message));
        }
    }

    public static void startIRC() throws Exception {
        if (bot == null) {
            //Configure what we want our bot to do

            Configuration configuration = new Configuration.Builder()
                    .setName(currentID) //Set the nick of the bot. CHANGE IN YOUR CODE
                    .addServer("irc.abcoc.cn") //Join the freenode network
                    .addAutoJoinChannel("#SoarClient") //Join the official #pircbotx channel
                    .addListener(new MyBot()) //Add our listener that will be called on Events
                    .buildConfiguration();

            //Create our bot with the configuration
            bot = new PircBotX(configuration);
            //Connect to the server
            running = true;
            botThread = new Thread(() -> {
                try {
                    bot.startBot();
                } catch (Exception e) {
                    if (running) {
                        SoarLogger.error("Failed to start IRC bot", e);
                    }
                }
            });
            botThread.start();
        }
    }

    public static void stopBot() {
        if (bot != null) {
            try {

                bot.stopBotReconnect();
                bot.close();
                if (botThread != null) {
                    botThread.interrupt();
                    botThread.join();
                }
                running = false;
            } catch (Exception e) {
                SoarLogger.error("Failed to stop IRC bot", e);
            } finally {
                bot = null;
                botThread = null;
            }
        }
    }

    public static void sendMessage(String message) {
        //Send a message to the channel
        if (bot != null) {
            bot.sendIRC().message("#SoarClient", message);
            if (currentID.equals("ABCOC")) {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[IRC] " + EnumChatFormatting.DARK_PURPLE + "[管理员]" + EnumChatFormatting.RED + currentID + EnumChatFormatting.WHITE + ": " + message));
            } else {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[IRC] " + EnumChatFormatting.WHITE + currentID + ": " + message));
            }
        }
    }
}