package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;

public class OldAnimationsMod extends Mod {

	private static OldAnimationsMod instance;
	
	private BooleanSetting blockHitSetting = new BooleanSetting(TranslateText.BLOCK_HIT, this, true);
	private BooleanSetting pushingSetting = new BooleanSetting(TranslateText.PUSHING, this, true);
	private BooleanSetting pushingParticleSetting = new BooleanSetting(TranslateText.PUSHING_PARTICLES, this, true);
	private BooleanSetting sneakSetting = new BooleanSetting(TranslateText.SNEAK, this, true);
	private BooleanSetting healthSetting = new BooleanSetting(TranslateText.HEALTH, this, true);
	
	private BooleanSetting armorDamageSetting = new BooleanSetting(TranslateText.ARMOR_DAMAGE, this, false);
	private BooleanSetting itemSwitchSetting = new BooleanSetting(TranslateText.ITEM_SWITCH, this, false);
	private BooleanSetting rodSetting = new BooleanSetting(TranslateText.ROD, this, false);
	
	public OldAnimationsMod() {
		super(TranslateText.OLD_ANIMATION, TranslateText.OLD_ANIMATION_DESCRIPTION, ModCategory.RENDER);
		
		instance = this;
	}

	public static OldAnimationsMod getInstance() {
		return instance;
	}

	public BooleanSetting getBlockHitSetting() {
		return blockHitSetting;
	}

	public BooleanSetting getPushingSetting() {
		return pushingSetting;
	}

	public BooleanSetting getPushingParticleSetting() {
		return pushingParticleSetting;
	}

	public BooleanSetting getSneakSetting() {
		return sneakSetting;
	}

	public BooleanSetting getHealthSetting() {
		return healthSetting;
	}

	public BooleanSetting getArmorDamageSetting() {
		return armorDamageSetting;
	}

	public BooleanSetting getItemSwitchSetting() {
		return itemSwitchSetting;
	}

	public BooleanSetting getRodSetting() {
		return rodSetting;
	}
}
