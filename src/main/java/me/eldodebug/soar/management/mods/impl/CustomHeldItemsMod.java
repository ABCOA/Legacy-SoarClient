package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRenderItemInFirstPerson;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;

public class CustomHeldItemsMod extends Mod {

	private NumberSetting xSetting = new NumberSetting(TranslateText.X, this, 0.75, -1, 1, false);
	private NumberSetting ySetting = new NumberSetting(TranslateText.Y, this, -0.15, -1, 1, false);
	private NumberSetting zSetting = new NumberSetting(TranslateText.Z, this, -1, -1, 1, false);
	
	private NumberSetting xScaleSetting = new NumberSetting(TranslateText.X_SCALE, this, 1, 0, 1, false);
	private NumberSetting yScaleSetting = new NumberSetting(TranslateText.Y_SCALE, this, 1, 0, 1, false);
	private NumberSetting zScaleSetting = new NumberSetting(TranslateText.Z_SCALE, this, 1, 0, 1, false);
	
	public CustomHeldItemsMod() {
		super(TranslateText.CUSTOM_HELD_ITEMS, TranslateText.CUSTOM_HELD_ITEMS_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onRenderItemInFirstPerson(EventRenderItemInFirstPerson event) {
		GlStateManager.translate(xSetting.getValueFloat(), ySetting.getValueFloat(), zSetting.getValueFloat());
		GlStateManager.scale(xScaleSetting.getValueFloat(), yScaleSetting.getValueFloat(), zScaleSetting.getValueFloat());
	}
}
