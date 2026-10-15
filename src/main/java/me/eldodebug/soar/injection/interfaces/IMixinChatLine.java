package me.eldodebug.soar.injection.interfaces;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public interface IMixinChatLine {
    HashSet<WeakReference<ChatLine>> chatLines = new HashSet<>();
    NetworkPlayerInfo getPlayerInfo();
    me.eldodebug.soar.hooks.OnlineChatMetadata.Sender getOnlineSender();
}
