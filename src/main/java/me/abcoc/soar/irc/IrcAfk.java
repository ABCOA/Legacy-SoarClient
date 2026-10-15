package me.abcoc.soar.irc;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.IRCMod;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

public final class IrcAfk {
    private static LongSupplier clock = System::nanoTime;
    private static BooleanSupplier heldInput = IrcAfk::hasHeldInput;
    private static boolean initialized, away, ignoreHeldUntilRelease;
    private static long lastActivity, syncedEpoch;
    private static ChatClient syncedClient;
    private static boolean syncedAway;

    private IrcAfk() {}

    public static void reset() {
        initialized = away = ignoreHeldUntilRelease = false;
        lastActivity = syncedEpoch = 0;
        syncedClient = null;
        syncedAway = false;
    }

    public static boolean isAway() { return away; }

    public static void onWorldLoaded() {
        if (initialized) lastActivity = clock.getAsLong();
    }

    public static void onInput() {
        if (!ChatClientManager.isEnabled() || !ChatClientManager.isInWorld()) return;
        initialized = true;
        lastActivity = clock.getAsLong();
        ignoreHeldUntilRelease = false;
        changeAway(false);
    }

    public static void setManual(boolean value) {
        ChatClientManager.updateConnectionState();
        if (!ChatClientManager.isEnabled()) { IrcFeatures.print("§c请先在功能菜单中开启 IRC。"); return; }
        if (!ChatClientManager.isInWorld()) { IrcFeatures.print("§e进入世界或连接游戏服务器后才能设置 AFK 状态。"); return; }
        initialized = true;
        lastActivity = clock.getAsLong();
        // The Enter key used to submit 'afk on' must not immediately mark the player as back.
        ignoreHeldUntilRelease = value;
        changeAway(value);
        sync();
    }

    public static void tick() {
        if (!ChatClientManager.isEnabled() || !ChatClientManager.isGameSessionActive()) { reset(); return; }
        long now = clock.getAsLong();
        if (!ChatClientManager.isInWorld()) {
            // Preserve presence across proxy switches, but do not count terrain loading as inactivity.
            if (initialized) lastActivity = now;
            return;
        }
        if (!initialized) { initialized = true; lastActivity = now; }
        boolean held = heldInput.getAsBoolean();
        if (ignoreHeldUntilRelease) {
            if (!held) ignoreHeldUntilRelease = false;
        } else if (held) {
            lastActivity = now;
            changeAway(false);
        }
        IRCMod mod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());
        if (mod.isAutoAfk() && !away && now - lastActivity >= TimeUnit.MINUTES.toNanos(mod.getAfkMinutes())) changeAway(true);
        sync();
    }

    private static void changeAway(boolean value) {
        if (away == value) return;
        away = value;
        IRCMod mod = (IRCMod) Soar.getInstance().getModManager().getModByTranslateKey(TranslateText.IRC.getKey());
        if (mod != null && !mod.isShowAfkNotices()) return;
        String name = Minecraft.getMinecraft().getSession().getUsername();
        IrcFeatures.print("§d" + name + (value ? " §7暂时离开了…" : " §e回来了…"));
    }

    private static void sync() {
        if (!ChatClientManager.serverAvailable()) return;
        ChatClient client = ChatClientManager.getChatClient();
        if (client == null) return;
        long epoch = client.getConnectionId();
        if (client != syncedClient || epoch != syncedEpoch) {
            syncedClient = client;
            syncedEpoch = epoch;
            // Login already establishes active presence; avoid broadcasting 'back' on every join.
            syncedAway = false;
        }
        if (away != syncedAway && ChatClientManager.sendFor(client, "afk", Boolean.toString(away))) syncedAway = away;
    }

    public static boolean isOwnPacket(ChatPacket packet) {
        ChatClient client = ChatClientManager.getChatClient();
        return client != null && client.getPlayerName().equals(packet.sender)
                && client.getPlayerUuid().equalsIgnoreCase(packet.senderUUID.replace("-", ""));
    }

    private static boolean hasHeldInput() {
        if (!Display.isCreated() || !Display.isActive()) return false;
        if (Keyboard.isCreated()) {
            for (int key = 1; key < Keyboard.KEYBOARD_SIZE; key++) if (Keyboard.isKeyDown(key)) return true;
        }
        if (Mouse.isCreated()) {
            for (int button = 0; button < Mouse.getButtonCount(); button++) if (Mouse.isButtonDown(button)) return true;
        }
        return false;
    }
}
