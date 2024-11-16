package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;

public class ClearGlassMod extends Mod {
	
	private static ClearGlassMod instance;

	private BooleanSetting normalSetting = new BooleanSetting(TranslateText.NORMAL, this, true);
	private BooleanSetting stainedSetting = new BooleanSetting(TranslateText.STAINED, this, true);
	
	private boolean prevNormal, prevStained;
	
	public ClearGlassMod() {
		super(TranslateText.CLEAR_GLASS, TranslateText.CLEAR_GLASS_DESCRIPTION, ModCategory.RENDER);
		
		instance = this;
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		
		if(prevNormal != normalSetting.isToggled()) {
			prevNormal = normalSetting.isToggled();
			mc.renderGlobal.loadRenderers();
		}
		
		if(prevStained != stainedSetting.isToggled()) {
			prevStained = stainedSetting.isToggled();
			mc.renderGlobal.loadRenderers();
		}
	}
	
	@Override
	public void onEnable() {
		prevNormal = normalSetting.isToggled();
		prevStained = stainedSetting.isToggled();
		super.onEnable();
		mc.renderGlobal.loadRenderers();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		mc.renderGlobal.loadRenderers();
	}
	
	public static ClearGlassMod getInstance() {
		return instance;
	}

	public BooleanSetting getNormalSetting() {
		return normalSetting;
	}

	public BooleanSetting getStainedSetting() {
		return stainedSetting;
	}
}
