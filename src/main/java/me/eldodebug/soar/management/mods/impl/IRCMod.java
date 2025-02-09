package me.eldodebug.soar.management.mods.impl;

import me.abcoc.soar.irc.ChatClient;
import me.abcoc.soar.irc.ChatClientManager;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;

public class IRCMod extends Mod {

    private BooleanSetting closeTips = new BooleanSetting(TranslateText.CLOSE_TIPS, this, false);

    public IRCMod() {
        super(TranslateText.IRC, TranslateText.IRC_DESCRIPTION, ModCategory.OTHER);
    }

    public ChatClient chatClient = null;
    @Override
    public void onEnable() {
        super.onEnable();
        try {
            chatClient = ChatClientManager.getChatClient();
        } catch (Exception e) {
            SoarLogger.error("Failed to start IRC bot", e);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (chatClient != null) {
            chatClient.stopIRC();
        }
    }

    public boolean isCloseTips() {
        return closeTips.isToggled();
    }

}
