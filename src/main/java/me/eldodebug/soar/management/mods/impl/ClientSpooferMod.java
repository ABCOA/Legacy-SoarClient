package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class ClientSpooferMod extends Mod {

	private static ClientSpooferMod instance;
	
	private ComboSetting typeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.VANILLA, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.VANILLA), new Option(TranslateText.FORGE))));
	
	public ClientSpooferMod() {
		super(TranslateText.CLIENT_SPOOFER, TranslateText.CLIENT_SPOOFER_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}

	public static ClientSpooferMod getInstance() {
		return instance;
	}

	public ComboSetting getTypeSetting() {
		return typeSetting;
	}
}
