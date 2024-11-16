package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class DamageTiltMod extends Mod {

	private static DamageTiltMod instance;
	
	public DamageTiltMod() {
		super(TranslateText.DAMAGE_TILT, TranslateText.DAMAGE_TILT_DESCRIPTION, ModCategory.PLAYER);
		
		instance = this;
	}

	public static DamageTiltMod getInstance() {
		return instance;
	}
}
