package me.abcoc.soar.irc;

import me.abcoc.soar.utils.netwok.MinecraftUUIDFetcher;
import net.minecraft.util.ChatComponentText;

import static me.abcoc.soar.irc.ChatClient.mc;

public class ClientReceiveHandler {
    public void handle(ChatPacket packet, ChatClientListener sender) {
        boolean isOfflinePlayer = true;
        // {"message":"SB","packetType":"chat","sender":"5j_XiaoShadiao","senderUUID":"Player0"}
        // System.out.println("Received packet " + jo + " from " + socket.getInetAddress());
        if (packet.packetType.equals("chat")) {
            isOfflinePlayer = packet.senderUUID.equals(MinecraftUUIDFetcher.getUUID(packet.sender)) ? false : true;
        }
        switch(packet.packetType) {
            case "chat":
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] " + (isOfflinePlayer ? "§c[离线]" : packet.getRank(true)) + "§d" + packet.sender + "§7: §f" + packet.message));
//                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[XSDChat] 请使用/xsdc message聊天!"));

                // 小沙雕音乐分享码: b1ZQ
                /*if(packet.message.startsWith("小沙雕音乐分享码:")) {
                    EventMusicShare.eventMusicShare.onChatEventMessage(packet.message);
                }
                */
                break;
            case "system":
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] §c[SYSTEM] §f" + packet.message));
//                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[XSDChat] 请使用/xsdc message聊天!"));
                break;
            case "join":
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] §7[§b+§7] " + packet.getRank(true) + "§b" + packet.sender + "..."));
//                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[XSDChat] 请使用/xsdc message聊天!"));
                break;
            case "leave":
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] §7[§c-§7] " + packet.getRank(true) + "§c" + packet.sender + "..."));
                break;
            case "heartbeat":
                sender.flagHeartbeat();
                break;
            case "tip":
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§a[SoarChat] 请使用.soarcmd irc message聊天"));
                break;
        }
    }
}
