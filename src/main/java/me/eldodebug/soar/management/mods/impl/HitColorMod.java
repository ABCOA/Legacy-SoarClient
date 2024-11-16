package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventHitOverlay;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ColorSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class HitColorMod extends Mod {

	private BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
	private ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, new Color(255, 0, 0), false);
    private NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 0.45, 0, 1.0, false);
    
	public HitColorMod() {
		super(TranslateText.HIT_COLOR, TranslateText.HIT_COLOR_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onHitOverlay(EventHitOverlay event) {
		
		AccentColor currentColor = Soar.getInstance().getColorManager().getCurrentColor();
		Color lastColor = customColorSetting.isToggled() ? colorSetting.getColor() : currentColor.getInterpolateColor();
		
		event.setRed(lastColor.getRed() / 255F);
		event.setGreen(lastColor.getGreen() / 255F);
		event.setBlue(lastColor.getBlue() / 255F);
		event.setAlpha(alphaSetting.getValueFloat());
	}
}
