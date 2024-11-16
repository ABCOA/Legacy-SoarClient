package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ColorSetting;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.Render3DUtils;
import me.eldodebug.soar.utils.TargetUtils;
import net.minecraft.client.renderer.GlStateManager;

public class TargetIndicatorMod extends Mod {

	private BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
	private ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, Color.RED, false);
    
	public TargetIndicatorMod() {
		super(TranslateText.TARGET_INDICATOR, TranslateText.TARGET_INDICATOR_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onRender3D(EventRender3D event) {
		
		AccentColor currentColor = Soar.getInstance().getColorManager().getCurrentColor();
		
		if(TargetUtils.getTarget() != null && !TargetUtils.getTarget().equals(mc.thePlayer)) {
			Render3DUtils.drawTargetIndicator(TargetUtils.getTarget(), 0.67, customColorSetting.isToggled() ? ColorUtils.applyAlpha(colorSetting.getColor(), 255) : currentColor.getInterpolateColor());
			GlStateManager.enableBlend();
		}
	}
}
