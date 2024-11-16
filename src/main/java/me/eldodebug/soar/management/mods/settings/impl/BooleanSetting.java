package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class BooleanSetting extends Setting {

	private boolean defaultValue, toggled;
	
	public BooleanSetting(TranslateText text, Mod parent, boolean toggled) {
		super(text, parent);
		
		this.toggled = toggled;
		this.defaultValue = toggled;
		
		Soar.getInstance().getModManager().addSettings(this);
	}
	
	@Override
	public void reset() {
		this.toggled = defaultValue;
	}
	
	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggle) {
		this.toggled = toggle;
	}

	public boolean isDefaultValue() {
		return defaultValue;
	}
}
