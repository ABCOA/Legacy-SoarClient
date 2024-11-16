package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventWaterOverlay;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class ClearWaterMod extends Mod {
	
	public ClearWaterMod() {
		super(TranslateText.CLEAR_WATER, TranslateText.CLEAR_WATER_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onWaterOverlay(EventWaterOverlay event) {
		event.setCancelled(true);
	}
}
