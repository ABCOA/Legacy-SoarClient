package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class GlobalSettingsMod extends Mod {

	private static GlobalSettingsMod instance;
	
	private ComboSetting modThemeSetting = new ComboSetting(TranslateText.HUD_THEME, this, TranslateText.NORMAL, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NORMAL), new Option(TranslateText.GLOW), new Option(TranslateText.OUTLINE), new Option(TranslateText.VANILLA),
			new Option(TranslateText.OUTLINE_GLOW), new Option(TranslateText.VANILLA_GLOW), new Option(TranslateText.SHADOW),
			new Option(TranslateText.DARK), new Option(TranslateText.LIGHT))));
	
	private NumberSetting volumeSetting = new NumberSetting(TranslateText.VOLUME, this, 0.8, 0, 1, false);
	private KeybindSetting modMenuKeybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_RSHIFT);
	
	public GlobalSettingsMod() {
		super(TranslateText.NONE, TranslateText.NONE, ModCategory.OTHER);
		
		instance = this;
	}

	@Override
	public void setup() {
		this.setHide(true);
		this.setToggled(true);
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		if(event.getKeyCode() == modMenuKeybindSetting.getKeyCode()) {
			mc.displayGuiScreen(Soar.getInstance().getApi().getModMenu());
		}
	}

	public static GlobalSettingsMod getInstance() {
		return instance;
	}

	public NumberSetting getVolumeSetting() {
		return volumeSetting;
	}

	public ComboSetting getModThemeSetting() {
		return modThemeSetting;
	}
}
