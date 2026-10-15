package me.abcoc.soar.irc;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IrcUsers {
    private static final int PAGE_SIZE = 10;
    private static final Pattern COLORED_NAME = Pattern.compile("(?:^|\\s)§f([A-Za-z0-9_]{1,16})(?=\\s|$)");
    private static final Pattern PLAIN_USER = Pattern.compile("((?:\\[[^\\]\\r\\n]*\\]\\s*)*)([A-Za-z0-9_]{1,16})(?=\\s|$)");
    private static List<User> users = Collections.emptyList();
    private static boolean received;
    private static int requestedPage = 1;

    private IrcUsers() {}

    public static final class User {
        public final String name, rank;
        public final boolean skyDiao;

        private User(String name, String rank) {
            this.name = name;
            this.rank = rank;
            ChatPacket identity = new ChatPacket();
            identity.senderRankName = rank;
            skyDiao = identity.hasSkyDiaoRank();
        }
    }

    public static void clear() {
        users = Collections.emptyList();
        received = false;
        requestedPage = 1;
    }

    public static void request(int page, boolean refresh) {
        if (page < 1) throw new IllegalArgumentException("页码必须大于 0。");
        if (!refresh && received) {
            display(page);
            return;
        }
        requestedPage = page;
        if (!IrcFeatures.send("query_all_users", "")) requestedPage = 1;
    }

    public static void receive(String payload) {
        users = parse(payload);
        received = true;
        int page = requestedPage;
        requestedPage = 1;
        display(page);
    }

    private static void display(int page) {
        String self = Minecraft.getMinecraft().getSession().getUsername();
        for (IChatComponent line : format(users, page, self)) IrcFeatures.print(line);
    }

    public static List<User> parse(String payload) {
        String text = payload == null ? "" : payload.trim();
        List<User> result = new ArrayList<User>();
        Matcher names = COLORED_NAME.matcher(text);
        int rankStart = 0, inspected = 0, brackets = 0;
        while (names.find()) {
            for (int i = inspected; i < names.start(); i++) {
                if (text.charAt(i) == '[') brackets++;
                else if (text.charAt(i) == ']') brackets = Math.max(0, brackets - 1);
            }
            inspected = names.end();
            if (brackets != 0) continue;
            result.add(new User(names.group(1), text.substring(rankStart, names.start()).trim()));
            rankStart = names.end();
        }
        if (result.isEmpty()) {
            Matcher plain = PLAIN_USER.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(text));
            while (plain.find()) result.add(new User(plain.group(2), plain.group(1).trim()));
        }
        result.sort(Comparator.comparing((User user) -> user.skyDiao)
                .thenComparing(user -> user.name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(user -> user.name));
        return result;
    }

    public static List<IChatComponent> format(List<User> snapshot, int requested, String self) {
        List<IChatComponent> lines = new ArrayList<IChatComponent>();
        int skyDiao = 0;
        for (User user : snapshot) if (user.skyDiao) skyDiao++;
        int other = snapshot.size() - skyDiao;
        int pages = Math.max(1, (snapshot.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(1, Math.min(requested, pages));
        lines.add(new ChatComponentText("§a[SoarChat] §bIRC 在线玩家 §7· §f" + snapshot.size() + " 人"));
        lines.add(new ChatComponentText("§7  PvP / 其他: §f" + other + " 人 §8| §7Skb: §f" + skyDiao + " 人"));
        if (snapshot.isEmpty()) lines.add(new ChatComponentText("§7  当前没有在线玩家。"));
        int start = (page - 1) * PAGE_SIZE, end = Math.min(start + PAGE_SIZE, snapshot.size());
        Boolean group = null;
        for (int i = start; i < end; i++) {
            User user = snapshot.get(i);
            if (group == null || group.booleanValue() != user.skyDiao) {
                group = user.skyDiao;
                lines.add(new ChatComponentText(user.skyDiao
                        ? "§6  Skb 客户端 §7(" + skyDiao + " 人)"
                        : "§b  PvP / 其他客户端 §7(" + other + " 人)"));
            }
            boolean own = user.name.equalsIgnoreCase(self);
            lines.add(new ChatComponentText("§8  · " + (user.rank.isEmpty() ? "" : "§r" + user.rank + " §r")
                    + (own ? "§a" : "§f") + user.name + (own ? " §a(你)" : "")));
        }
        IChatComponent navigation = new ChatComponentText("§7  ");
        navigation.appendSibling(button("[上一页]", ".irc users " + (page - 1), page > 1));
        navigation.appendText(" §f" + page + "§7/§f" + pages + " §7");
        navigation.appendSibling(button("[下一页]", ".irc users " + (page + 1), page < pages));
        navigation.appendText("  ");
        navigation.appendSibling(button("[刷新]", ".irc users", true));
        lines.add(navigation);
        return lines;
    }

    private static IChatComponent button(String label, String command, boolean enabled) {
        ChatStyle style = new ChatStyle().setColor(enabled ? EnumChatFormatting.AQUA : EnumChatFormatting.DARK_GRAY);
        if (enabled) style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return new ChatComponentText(label).setChatStyle(style);
    }
}
