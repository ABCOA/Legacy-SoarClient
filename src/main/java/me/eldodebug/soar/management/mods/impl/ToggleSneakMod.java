package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;

public class ToggleSneakMod extends Mod {

	private boolean toggle;
	
	public ToggleSneakMod() {
		super(TranslateText.TOGGLE_SNEAK, TranslateText.TOGGLE_SNEAK_DESCRIPTION, ModCategory.PLAYER);
	}

	@Override
	public void setup() {
		toggle = false;
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		if(event.getKeyCode() == mc.gameSettings.keyBindSneak.getKeyCode()) {
			toggle = !toggle;
		}
	}
	
	@EventTarget
	public void onUpdate(EventUpdate event) {
		if(mc.currentScreen instanceof Gui) {
			setSneak(false);
		}else {
			setSneak(toggle);
		}
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		toggle = false;
		setSneak(false);
	}
	
	private void setSneak(boolean state) {
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), state);
	}
}
