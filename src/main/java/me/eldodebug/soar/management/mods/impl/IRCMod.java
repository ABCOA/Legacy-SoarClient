package me.eldodebug.soar.management.mods.impl;

import me.abcoc.soar.irc.ChatClientManager;
import me.abcoc.soar.irc.IrcAfk;
import me.abcoc.soar.irc.IrcFeatures;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.Arrays;

public class IRCMod extends Mod {

    private BooleanSetting closeTips = new BooleanSetting(TranslateText.CLOSE_TIPS, this, false);
    private ComboSetting messageSource = new ComboSetting(TranslateText.IRC_MESSAGE_SOURCE, this, TranslateText.ALL,
            new ArrayList<Option>(Arrays.asList(new Option(TranslateText.ALL), new Option(TranslateText.IRC_PVP_ONLY))));
    private BooleanSetting showAfkNotices = new BooleanSetting(TranslateText.IRC_SHOW_AFK_NOTICES, this, true);
    private BooleanSetting autoAfk = new BooleanSetting(TranslateText.AUTO_AFK, this, true);
    private NumberSetting afkMinutes = new NumberSetting(TranslateText.AFK_MINUTES, this, 5, 1, 30, true);

    public IRCMod() {
        super(TranslateText.IRC, TranslateText.IRC_DESCRIPTION, ModCategory.OTHER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ChatClientManager.updateConnectionState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ChatClientManager.stopAndClear();
        IrcAfk.reset();
    }

    public boolean isCloseTips() {
        return closeTips.isToggled();
    }

    public boolean isAutoAfk() { return autoAfk.isToggled(); }
    public boolean isPvpOnly() { return messageSource.getOption().getTranslate() == TranslateText.IRC_PVP_ONLY; }
    public boolean isShowAfkNotices() { return showAfkNotices.isToggled(); }
    public int getAfkMinutes() { return Math.max(1, Math.min(30, afkMinutes.getValueInt())); }

    @EventTarget public void onTick(EventTick event) {
        ChatClientManager.updateConnectionState();
        IrcAfk.tick();
        IrcFeatures.tick();
    }

    @EventTarget public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof S02PacketChat) {
            final String text = ((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText();
            final Object world = mc.theWorld;
            mc.addScheduledTask(() -> {
                if (isToggled() && mc.theWorld == world && ChatClientManager.isInWorld()) IrcFeatures.receiveGameChat(text);
            });
        }
    }

}
