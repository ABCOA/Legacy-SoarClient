package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class HitDelayFixMod extends Mod {

	private static HitDelayFixMod instance;
	
	public HitDelayFixMod() {
		super(TranslateText.HIT_DELAY_FIX, TranslateText.HIT_DELAY_FIX_DESCRIPTION, ModCategory.PLAYER);
		
		instance = this;
	}

	public static HitDelayFixMod getInstance() {
		return instance;
	}
}
