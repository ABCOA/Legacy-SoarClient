package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.Arrays;

public class ChatTranslateMod extends Mod {

	private static ChatTranslateMod instance;
	
	private ComboSetting languageSetting = new ComboSetting(TranslateText.LANGUAGE, this, TranslateText.JAPANESE, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.JAPANESE), new Option(TranslateText.ENGLISH), new Option(TranslateText.CHINESE), new Option(TranslateText.POLISH))));
	
	public ChatTranslateMod() {
		super(TranslateText.CHAT_TRANSLATE, TranslateText.CHAT_TRANSLATE_DESCRIPTION, ModCategory.OTHER);
		
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
	
	public static ChatTranslateMod getInstance() {
		return instance;
	}

	public ComboSetting getLanguageSetting() {
		return languageSetting;
	}
}
