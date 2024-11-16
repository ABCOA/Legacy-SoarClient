package me.eldodebug.soar.management.mods.impl;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;

public class AutoTextMod extends Mod {

	private KeybindSetting text1KeybindSetting = new KeybindSetting(TranslateText.TEXT_1_KEY, this, Keyboard.KEY_NONE);
	private TextSetting text1Setting = new TextSetting(TranslateText.TEXT_1, this, "");
	private KeybindSetting text2KeybindSetting = new KeybindSetting(TranslateText.TEXT_2_KEY, this, Keyboard.KEY_NONE);
	private TextSetting text2Setting = new TextSetting(TranslateText.TEXT_2, this, "");
	private KeybindSetting text3KeybindSetting = new KeybindSetting(TranslateText.TEXT_3_KEY, this, Keyboard.KEY_NONE);
	private TextSetting text3Setting = new TextSetting(TranslateText.TEXT_3, this, "");
	
	public AutoTextMod() {
		super(TranslateText.AUTO_TEXT, TranslateText.AUTO_TEXT_DESCRIPTION, ModCategory.PLAYER);
	}

	@EventTarget
	public void onKey(EventKey event) {
		
		if(event.getKeyCode() == text1KeybindSetting.getKeyCode()) {
			mc.thePlayer.sendChatMessage(text1Setting.getText());
		}
		
		if(event.getKeyCode() == text2KeybindSetting.getKeyCode()) {
			mc.thePlayer.sendChatMessage(text2Setting.getText());
		}
		
		if(event.getKeyCode() == text3KeybindSetting.getKeyCode()) {
			mc.thePlayer.sendChatMessage(text3Setting.getText());
		}
	}
}
