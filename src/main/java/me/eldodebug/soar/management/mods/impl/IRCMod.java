package me.eldodebug.soar.management.mods.impl;

import me.abcoc.soar.irc.MyBot;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class IRCMod extends Mod {

    public IRCMod() {
        super(TranslateText.IRC, TranslateText.IRC_DESCRIPTION, ModCategory.OTHER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            MyBot.startIRC();
        } catch (Exception e) {
            SoarLogger.error("Failed to start IRC bot", e);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MyBot.stopBot();
    }

}
