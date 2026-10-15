package me.eldodebug.soar.management.command.impl;

import me.abcoc.soar.irc.*;
import me.eldodebug.soar.management.command.Command;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IRCCommand extends Command {
    public IRCCommand() { super("irc"); }
    @Override public void onCommand(String message) {
        String text = message == null ? "" : message.trim();
        String[] args = text.isEmpty() ? new String[0] : text.split("\\s+");
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) { help(); return; }
            switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
                case "send": IrcFeatures.send("chat", tail(text)); break;
                case "status":
                    if (ChatClientManager.isEnabled() && !ChatClientManager.isGameSessionActive()) {
                        IrcFeatures.print("§eIRC 已开启，等待进入世界或游戏服务器。");
                        break;
                    }
                    ChatClient client = ChatClientManager.getChatClient();
                    IrcFeatures.print((ChatClientManager.serverAvailable() ? "§a已连接 " : "§e未连接 ")
                            + IrcConfig.HOST + (client == null || client.getLastError().isEmpty() ? "" : " (" + client.getLastError() + ")"));
                    break;
                case "reconnect":
                    if (!ChatClientManager.isEnabled()) { IrcFeatures.print("§c请先开启 IRC。"); break; }
                    if (!ChatClientManager.isGameSessionActive()) { IrcFeatures.print("§eIRC 会在进入世界或游戏服务器后自动连接。"); break; }
                    ChatClientManager.refreshChatClient();
                    IrcFeatures.print("§e已重新发起连接。");
                    break;
                case "server":
                    IrcFeatures.print("§eIRC 使用固定服务地址，无需配置。");
                    break;
                case "users":
                    require(args.length <= 2 && (args.length == 1 || args[1].matches("[0-9]+")), "users [页码]");
                    int page;
                    try { page = args.length == 1 ? 1 : Integer.parseInt(args[1]); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("页码过大。"); }
                    IrcUsers.request(page, args.length == 1);
                    break;
                case "afk":
                    require(args.length == 2, "afk on|off");
                    IrcAfk.setManual(enabled(args[1]));
                    break;
                case "item": IrcFeatures.showItem(); break;
                case "stats":
                    require(args.length == 2 && args[1].matches("[A-Za-z0-9_]{1,16}"), "stats <玩家名>");
                    IrcFeatures.send("lps", args[1]);
                    break;
                case "pt": IrcFeatures.send("chat", "/pt"); break;
                case "uuid":
                    require(args.length == 2, "uuid <玩家名>");
                    IrcFeatures.send("chat", "/checkuuid " + args[1]);
                    break;
                case "bind":
                    require(args.length == 2 && args[1].matches("[0-9]{5,12}"), "bind <QQ号>");
                    if (IrcFeatures.send("challenge_mc", Long.toString(Long.parseLong(args[1]))))
                        IrcFeatures.print("§e已发送绑定请求，需通过服务端 QQ 验证完成绑定。");
                    break;
                case "location": location(args); break;
                case "party": party(args); break;
                case "macro":
                    require(args.length > 1, "macro <警报信息>");
                    IrcFeatures.send("macro_check", tail(text));
                    break;
                case "error":
                    IrcFeatures.send("last_error", Base64.getEncoder().encodeToString(tail(text).getBytes(StandardCharsets.UTF_8)));
                    break;
                case "packet":
                    String[] raw = text.split("\\s+", 3);
                    require(raw.length == 3 && raw[1].matches("[a-z_]+"), "packet <消息类型> <原始载荷>");
                    IrcFeatures.send(raw[1], raw[2]);
                    break;
                default: IrcFeatures.send("chat", text);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            IrcFeatures.print("§c" + e.getMessage());
        }
    }
    private static String tail(String text) {
        int space = 0;
        while (space < text.length() && !Character.isWhitespace(text.charAt(space))) space++;
        return space == text.length() ? "" : text.substring(space).trim();
    }
    private static void require(boolean condition, String syntax) {
        if (!condition) throw new IllegalArgumentException("用法: .irc " + syntax);
    }
    private static boolean enabled(String value) {
        if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) return true;
        if ("off".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("请输入 on 或 off");
    }
    private static void location(String[] args) {
        IrcConfig config = ChatClientManager.getConfig();
        if (args.length == 2) {
            config.shareLocation = enabled(args[1]);
            config.save();
            IrcFeatures.print("§e位置同步已" + (config.shareLocation ? "开启；请确认服务端已识别 Soar，否则可能被断开连接。" : "关闭。"));
        } else {
            require(args.length == 6 && args[1].equalsIgnoreCase("set"), "location on|off 或 location set <serverID> <gameType> <lobbyName> <mode>");
            require(config.shareLocation, "location on (需要服务端识别 Soar)");
            IrcFeatures.setLocation(args[2], args[3], args[4], args[5]);
        }
    }
    private static void party(String[] args) {
        IrcConfig config = ChatClientManager.getConfig();
        if (args.length == 1) { IrcFeatures.print("§e队伍成员(UUID=角色): " + IrcFeatures.getParty()); return; }
        if (args.length == 2 && (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off"))) {
            config.shareParty = enabled(args[1]);
            config.save();
            if (config.shareParty) IrcFeatures.publishParty();
            else if (ChatClientManager.serverAvailable()) IrcFeatures.send("hyp_party", IrcPayloads.party(false, new String[0], new int[0]));
            IrcFeatures.print("§e队伍同步已" + (config.shareParty ? "开启。" : "关闭。"));
        } else if (args.length == 2 && args[1].equalsIgnoreCase("leave")) {
            IrcFeatures.leaveParty();
        } else {
            require(args.length == 4 && args[1].equalsIgnoreCase("add"), "party on|off|leave 或 party add <UUID> <leader|mod|member>");
            require(config.shareParty, "party on");
            int role = args[3].equalsIgnoreCase("leader") ? 0 : args[3].equalsIgnoreCase("mod") ? 1 : args[3].equalsIgnoreCase("member") ? 2 : -1;
            IrcFeatures.partyMember(args[2], role);
        }
    }
    private static void help() {
        IrcFeatures.print("§b.irc <消息>§f 聊天；@AI ... 和 /服务端指令会原样发送。");
        IrcFeatures.print("§bstatus / reconnect / users [页码] / afk on|off");
        IrcFeatures.print("§bitem / stats <玩家名> / pt / uuid <玩家名> / bind <QQ号>");
        IrcFeatures.print("§blocation on|off / party on|off / party add <UUID> <leader|mod|member> / party leave");
        IrcFeatures.print("§bmacro <信息> / error <信息>");
    }
}
