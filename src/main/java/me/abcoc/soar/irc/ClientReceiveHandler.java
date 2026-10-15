package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.event.impl.EventIRC;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;

public class ClientReceiveHandler {
    public static void handle(ChatPacket packet) {
        if ("glacite_mineshaft_share".equals(packet.packetType) || "slayer_together".equals(packet.packetType)
                || "ysm".equals(packet.packetType)) return;
        if (new EventIRC(packet).call().isCancelled()) return;
        IRCMod mod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());
        if (mod != null && (("afk".equals(packet.packetType) && !mod.isShowAfkNotices())
                || (mod.isPvpOnly() && packet.hasSkyDiaoRank()))) return;
        try {
        switch(packet.packetType) {
            case "chat":
                IrcFeatures.print(packet.getRank(true) + "§d" + packet.sender + "§7: §f" + packet.message);
                break;
            case "system":
                IrcFeatures.print("§c[SYSTEM] §f" + packet.message);
                break;
            case "join":
                IrcFeatures.print("§7[§b+§7] " + packet.getRank(true) + "§b" + packet.sender + " 加入了聊天室");
                break;
            case "leave":
                IrcFeatures.print("§7[§c-§7] " + packet.getRank(true) + "§c" + packet.sender + " 离开了聊天室");
                break;
            case "heartbeat":
                break;
            case "tip":
                if (mod != null && !mod.isCloseTips()) IrcFeatures.print("§e使用 .irc help 查看 IRC 功能。");
                break;
            case "users_list":
                IrcUsers.receive(packet.message);
                break;
            case "afk":
                if (!IrcAfk.isOwnPacket(packet)) IrcFeatures.print(packet.getRank(true) + "§d" + packet.sender + (Boolean.parseBoolean(packet.message) ? " §7暂时离开了…" : " §e回来了…"));
                break;
            case "showitem":
                IrcFeatures.print(IrcMessages.item(packet));
                break;
            case "lps":
                IrcFeatures.print(IrcMessages.stats(packet.message));
                break;
            case "macro_check":
                IrcFeatures.print("§c[Macro 警报] §e" + packet.sender + "\n" + packet.message);
                break;
            case "hyp_party":
                IrcFeatures.print("§e队伍信息已更新。使用 .irc party 查看或同步队伍。");
                break;
            default:
                IrcFeatures.print("§7[" + packet.packetType + "] §f" + packet.message);
        }
        } catch (RuntimeException e) {
            SoarLogger.warn("Cannot display IRC packet " + packet.packetType + ": " + e.getMessage());
            IrcFeatures.print("§e收到无法解析的 " + packet.packetType + " 消息。");
        }
    }

}
