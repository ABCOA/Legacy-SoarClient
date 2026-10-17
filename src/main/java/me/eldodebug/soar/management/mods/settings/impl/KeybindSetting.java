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
        Object before = getKeyCode();

		this.keyCode = defaultKeyCode;
	
        if (!java.util.Objects.equals(before, getKeyCode())) changed();
    }
	
	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
        Object before = getKeyCode();

		this.keyCode = keyCode;
	
        if (!java.util.Objects.equals(before, getKeyCode())) changed();
    }

	public int getDefaultKeyCode() {
		return defaultKeyCode;
	}
	
	public boolean isKeyDown() {
		return Keyboard.isKeyDown(keyCode) && !(Minecraft.getMinecraft().currentScreen instanceof Gui);
	}
}
