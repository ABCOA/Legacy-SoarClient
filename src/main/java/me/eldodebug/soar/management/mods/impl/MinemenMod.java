package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.S02PacketChat;

public class MinemenMod extends Mod {

	private BooleanSetting autoPlaySetting = new BooleanSetting(TranslateText.AUTO_PLAY, this, false);
	
	public MinemenMod() {
		super(TranslateText.MINEMEN, TranslateText.MINEMEN_DESCRIPTION, ModCategory.OTHER);
	}

	@EventTarget
	public void onReceivePacket(EventReceivePacket event) {
		
		if(autoPlaySetting.isToggled() && event.getPacket() instanceof S02PacketChat) {
			
			S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
			String raw = chatPacket.getChatComponent().toString();
			
			if (raw.contains("clickEvent=ClickEvent{action=RUN_COMMAND, value='/requeue")) {
				mc.thePlayer.sendChatMessage("/requeue");
			}
		}
	}
}
