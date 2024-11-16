package me.eldodebug.soar.discord.ipc;

import com.google.gson.JsonObject;

import me.eldodebug.soar.discord.ipc.entities.Packet;
import me.eldodebug.soar.discord.ipc.entities.User;

public interface IPCListener {
	
    default void onPacketSent(IPCClient client, Packet packet) {}
    default void onPacketReceived(IPCClient client, Packet packet) {}
    default void onActivityJoin(IPCClient client, String secret) {}
    default void onActivitySpectate(IPCClient client, String secret) {}
    default void onActivityJoinRequest(IPCClient client, String secret, User user) {}
    default void onReady(IPCClient client) {}
    default void onClose(IPCClient client, JsonObject json) {}
    default void onDisconnect(IPCClient client, Throwable t) {}
}
