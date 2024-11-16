package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class WaveyCapesMod extends Mod {

	private static WaveyCapesMod instance;
	
	private NumberSetting gravitySetting = new NumberSetting(TranslateText.GRAVITY, this, 15, 2, 30, false);
	private ComboSetting movementSetting = new ComboSetting(TranslateText.MOVEMENT, this, TranslateText.BASIC, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.VANILLA), new Option(TranslateText.BASIC))));
			
	private ComboSetting styleSetting = new ComboSetting(TranslateText.STYLE, this, TranslateText.SMOOTH, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.BLOCKY), new Option(TranslateText.SMOOTH))));
	
	private ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.WAVES, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NONE), new Option(TranslateText.WAVES))));
	
	private NumberSetting heightMultiplierSetting = new NumberSetting(TranslateText.HEIGHT_MULTIPLIER, this, 6, 2, 10, true);
	
	public WaveyCapesMod() {
		super(TranslateText.WAVEY_CAPES, TranslateText.WAVEY_CAPES_DESCRIPTION, ModCategory.RENDER);
		
		instance = this;
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		if(MoBendsMod.getInstance().isToggled()) {
			MoBendsMod.getInstance().setToggled(false);
		}
	}

	public static WaveyCapesMod getInstance() {
		return instance;
	}

	public NumberSetting getGravitySetting() {
		return gravitySetting;
	}

	public ComboSetting getMovementSetting() {
		return movementSetting;
	}

	public ComboSetting getStyleSetting() {
		return styleSetting;
	}

	public ComboSetting getModeSetting() {
		return modeSetting;
	}

	public NumberSetting getHeightMultiplierSetting() {
		return heightMultiplierSetting;
	}
}
