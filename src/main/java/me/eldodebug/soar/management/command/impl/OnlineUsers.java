package me.eldodebug.soar.management.command.impl;

import me.abcoc.soar.irc.IrcFeatures;
import me.eldodebug.soar.management.command.Command;

public class OnlineUsers extends Command {
    public OnlineUsers() { super("users"); }
    @Override public void onCommand(String message) { IrcFeatures.send("query_all_users", ""); }
}
