package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import net.minecraft.network.play.server.S02PacketChat;

public class ChatCopyMod extends Mod {

    private static ChatCopyMod instance;

    public ChatCopyMod() {
        super(TranslateText.CHAT_COPY, TranslateText.CHAT_COPY_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if(event.getPacket() instanceof S02PacketChat) {
            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            event.setCancelled(true);
            ChatHandler.handleChat(chatPacket);
        }
    }

    public static ChatCopyMod getInstance() {
        return instance;
    }
}
