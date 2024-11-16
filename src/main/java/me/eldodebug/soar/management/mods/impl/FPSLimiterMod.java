package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class FPSLimiterMod extends Mod {

	private static FPSLimiterMod instance;
	
	private BooleanSetting limitMaxFpsSetting = new BooleanSetting(TranslateText.LIMIT_MAX_FPS, this, true);
	private NumberSetting maxFpsSetting = new NumberSetting(TranslateText.MAX_FPS, this, 480, 240, 1440, true);
	private BooleanSetting limitGuiFps = new BooleanSetting(TranslateText.LIMIT_GUI_FPS, this, true);
	private NumberSetting guiFpsSetting = new NumberSetting(TranslateText.GUI_FPS, this, 30, 1, 240, true);
	
	public FPSLimiterMod() {
		super(TranslateText.FPS_LIMITER, TranslateText.FPS_LIMITER_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}

	public static FPSLimiterMod getInstance() {
		return instance;
	}

	public NumberSetting getMaxFpsSetting() {
		return maxFpsSetting;
	}

	public BooleanSetting getLimitGuiFps() {
		return limitGuiFps;
	}

	public NumberSetting getGuiFpsSetting() {
		return guiFpsSetting;
	}

	public BooleanSetting getLimitMaxFpsSetting() {
		return limitMaxFpsSetting;
	}
}
