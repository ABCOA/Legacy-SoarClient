package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class WeatherChangerMod extends Mod {

	private static WeatherChangerMod instance;
	
	private ComboSetting weatherSetting = new ComboSetting(TranslateText.WEATHER, this, TranslateText.CLEAR, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.CLEAR), new Option(TranslateText.RAIN), new Option(TranslateText.STORM), new Option(TranslateText.SNOW))));
	
	private NumberSetting rainStrength = new NumberSetting(TranslateText.RAIN_STRENGTH, this, 1, 0, 1, false);
	private NumberSetting thunderStrength = new NumberSetting(TranslateText.THUNDER_STRENGTH, this, 1, 0, 1, false);
	
	public WeatherChangerMod() {
		super(TranslateText.WEATHER_CHANGER, TranslateText.WEATHER_CHANGER_DESCRIPTION, ModCategory.WORLD);
		
		instance = this;
	}

	public static WeatherChangerMod getInstance() {
		return instance;
	}

	public ComboSetting getWeatherSetting() {
		return weatherSetting;
	}

	public NumberSetting getRainStrength() {
		return rainStrength;
	}

	public NumberSetting getThunderStrength() {
		return thunderStrength;
	}
}
