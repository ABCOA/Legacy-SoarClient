package me.eldodebug.soar.management.command.impl;

import me.abcoc.soar.irc.MyBot;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.utils.Multithreading;

public class IRCCommand extends Command {

        public IRCCommand() {
            super("irc");
        }

        @Override
        public void onCommand(String message) {

            String text = message;

            Multithreading.runAsync(()-> {
                try {
                    MyBot.sendMessage(text);
                } catch (Exception e) {
                    SoarLogger.error("Failed to send message to IRC.", e);
                }
            });
        }
}
