package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class TaplookMod extends Mod {

	private boolean active;
	private int prevPerspective;
	
	private ComboSetting modeSetting = new ComboSetting(TranslateText.PERSPECTIVE, this, TranslateText.FRONT, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.FRONT), new Option(TranslateText.BEHIND))));
	
	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_P);
	
	public TaplookMod() {
		super(TranslateText.TAPLOOK, TranslateText.TAPLOOK_DESCRIPTION, ModCategory.PLAYER);
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		if(keybindSetting.isKeyDown()) {
			if(!active) {
				this.start();
			}
		}else if(active) {
			this.stop();
		}
	}
	
	private void start() {
		
		Option option = modeSetting.getOption();
		int perspective = option.getTranslate().equals(TranslateText.FRONT) ? 2 : 1;
		
		active = true;
		prevPerspective = mc.gameSettings.thirdPersonView;
		mc.gameSettings.thirdPersonView = perspective;
		mc.renderGlobal.setDisplayListEntitiesDirty();
	}
	
	private void stop() {
		active = false;
		mc.gameSettings.thirdPersonView = prevPerspective;
		mc.renderGlobal.setDisplayListEntitiesDirty();
	}
}
