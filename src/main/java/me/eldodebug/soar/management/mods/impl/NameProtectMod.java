package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventText;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;

public class NameProtectMod extends Mod {

	private TextSetting nameSetting = new TextSetting(TranslateText.NAME, this, "You");
	
	public NameProtectMod() {
		super(TranslateText.NAME_PROTECT, TranslateText.NAME_PROTECT_DESCRIPTION, ModCategory.PLAYER);
	}
	
	@EventTarget
	public void onText(EventText event) {
		event.replace(mc.getSession().getUsername(), nameSetting.getText());
	}
}
