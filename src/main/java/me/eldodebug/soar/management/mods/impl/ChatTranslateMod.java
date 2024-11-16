package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
			
			S02PacketChat chatPacket = (S02PacketChat)event.getPacket();
			IChatComponent translate = new ChatComponentText(" [" + String.valueOf('\u270E') + "]").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd translate " + chatPacket.getChatComponent().getUnformattedText()))
					.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_TRANSLATE.getText()))));
            final String chatMessage = chatPacket.getChatComponent().getUnformattedText();
            
            if(chatMessage.replaceAll(" ", "").isEmpty() || chatPacket.getType() == 2) {
            	return;
            }
            
			event.setCancelled(true);
            
			mc.ingameGUI.getChatGUI().printChatMessage(chatPacket.getChatComponent().appendSibling(translate));
		}
	}
	
	public static ChatTranslateMod getInstance() {
		return instance;
	}

	public ComboSetting getLanguageSetting() {
		return languageSetting;
	}
}
