package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventFireOverlay;
import me.eldodebug.soar.management.event.impl.EventRenderPumpkinOverlay;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;

public class OverlayEditorMod extends Mod {

	private BooleanSetting hidePumpkinSetting = new BooleanSetting(TranslateText.HIDE_PUMPKIN, this, false);
	private BooleanSetting hideFireSetting = new BooleanSetting(TranslateText.HIDE_FIRE, this, false);
	
	public OverlayEditorMod() {
		super(TranslateText.OVERLAY_EDITOR, TranslateText.OVERLAY_EDITOR_DESCRIPTION, ModCategory.RENDER);
	}
	
	@EventTarget
	public void onRenderPumpkinOverlay(EventRenderPumpkinOverlay event) {
		event.setCancelled(hidePumpkinSetting.isToggled());
	}
	
	@EventTarget
	public void onFireOverlay(EventFireOverlay event) {
		event.setCancelled(hideFireSetting.isToggled());
	}
}
