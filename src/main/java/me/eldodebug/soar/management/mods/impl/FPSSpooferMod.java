package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class FPSSpooferMod extends Mod {

	private static FPSSpooferMod instance;
	
	private NumberSetting multiplierSetting = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 30, true);
	
	public FPSSpooferMod() {
		super(TranslateText.FPS_SPOOFER, TranslateText.FPS_SPOOFER_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}

	public static FPSSpooferMod getInstance() {
		return instance;
	}

	public NumberSetting getMultiplierSetting() {
		return multiplierSetting;
	}
}
