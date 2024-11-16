package me.eldodebug.soar.management.mods.settings.impl;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class KeybindSetting extends Setting {

	private int defaultKeyCode, keyCode;
	
	public KeybindSetting(TranslateText text, Mod parent, int keyCode) {
		super(text, parent);
		this.defaultKeyCode = keyCode;
		this.keyCode = keyCode;
		
		Soar.getInstance().getModManager().addSettings(this);
	}

	@Override
	public void reset() {
		this.keyCode = defaultKeyCode;
	}
	
	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public int getDefaultKeyCode() {
		return defaultKeyCode;
	}
	
	public boolean isKeyDown() {
		return Keyboard.isKeyDown(keyCode) && !(Minecraft.getMinecraft().currentScreen instanceof Gui);
	}
}