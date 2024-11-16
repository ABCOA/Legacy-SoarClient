package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class SlowSwingMod extends Mod {

	private static SlowSwingMod instance;
	
	private NumberSetting delaySetting = new NumberSetting(TranslateText.DELAY, this, 14, 2, 20, true);
	
	public SlowSwingMod() {
		super(TranslateText.SLOW_SWING, TranslateText.SLOW_SWING_DESCRIPTION, ModCategory.PLAYER);
		
		instance = this;
	}

	public static SlowSwingMod getInstance() {
		return instance;
	}

	public NumberSetting getDelaySetting() {
		return delaySetting;
	}
}
