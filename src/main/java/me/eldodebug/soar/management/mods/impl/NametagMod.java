package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class NametagMod extends Mod {

	private static NametagMod instance;
	
	public NametagMod() {
		super(TranslateText.NAMETAG, TranslateText.NAMETAG_DESCRIPTION, ModCategory.PLAYER);
		
		instance = this;
	}

	public static NametagMod getInstance() {
		return instance;
	}
}
