package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventHurtCamera;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class MinimalDamageShakeMod extends Mod {

	private NumberSetting intensitySetting = new NumberSetting(TranslateText.INTENSITY, this, 0, 0, 100, true);
	
	public MinimalDamageShakeMod() {
		super(TranslateText.MINIMAL_DAMAGE_SHAKE, TranslateText.MINIMAL_DAMAGE_SHAKE_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onHurtCamera(EventHurtCamera event) {
		event.setIntensity(intensitySetting.getValueFloat() / 100F);
	}
}
