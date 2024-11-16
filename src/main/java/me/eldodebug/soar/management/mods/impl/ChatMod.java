package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class ChatMod extends Mod {

	private static ChatMod instance;
	
	private BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH, this, true);
	private NumberSetting smoothSpeedSetting = new NumberSetting(TranslateText.SMOOTH_SPEED, this, 4, 1, 10, true);
	private BooleanSetting headSetting = new BooleanSetting(TranslateText.HEAD, this, false);
	
	private BooleanSetting infinitySetting = new BooleanSetting(TranslateText.INFINITY, this, false);
	private BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);
	
	private BooleanSetting compactSetting = new BooleanSetting(TranslateText.COMPACT, this, false);
	
	public ChatMod() {
		super(TranslateText.CHAT, TranslateText.CHAT_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}

	public static ChatMod getInstance() {
		return instance;
	}

	public BooleanSetting getSmoothSetting() {
		return smoothSetting;
	}

	public NumberSetting getSmoothSpeedSetting() {
		return smoothSpeedSetting;
	}

	public BooleanSetting getHeadSetting() {
		return headSetting;
	}

	public BooleanSetting getInfinitySetting() {
		return infinitySetting;
	}

	public BooleanSetting getBackgroundSetting() {
		return backgroundSetting;
	}

	public BooleanSetting getCompactSetting() {
		return compactSetting;
	}
}
