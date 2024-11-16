package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventGamma;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class FullbrightMod extends Mod {

	public FullbrightMod() {
		super(TranslateText.FULLBRIGHT, TranslateText.FULLBRIGHT_DESCRIPTION, ModCategory.PLAYER);
	}

	@EventTarget
	public void onGamma(EventGamma event) {
		event.setGamma(20F);
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
        mc.renderGlobal.loadRenderers();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
        mc.renderGlobal.loadRenderers();
	}
}
