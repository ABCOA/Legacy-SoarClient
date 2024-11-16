package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class TimeChangerMod extends Mod {

	private static TimeChangerMod instance;
	
	private NumberSetting timeSetting = new NumberSetting(TranslateText.TIME, this, 12, 0, 24, false);
	
	public TimeChangerMod() {
		super(TranslateText.TIME_CHANGER, TranslateText.TIME_CHANGER_DESCRIPTION, ModCategory.WORLD);
		
		instance = this;
	}

	public static TimeChangerMod getInstance() {
		return instance;
	}

	public NumberSetting getTimeSetting() {
		return timeSetting;
	}

}
