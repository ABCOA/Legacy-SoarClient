package me.eldodebug.soar.management.event.impl;

import me.abcoc.soar.irc.ChatPacket;
import me.eldodebug.soar.management.event.Event;

public class EventIRC extends Event {
    private final ChatPacket packet;
    public EventIRC(ChatPacket packet) { this.packet = packet; }
    public ChatPacket getPacket() { return packet; }
}
