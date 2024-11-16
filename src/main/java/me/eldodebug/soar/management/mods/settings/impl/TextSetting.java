package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class TextSetting extends Setting {

	private String defaultText, text;
	
	public TextSetting(TranslateText tText, Mod parent, String text) {
		super(tText, parent);
		this.text = text;
		this.defaultText = text;
		
		Soar.getInstance().getModManager().addSettings(this);
	}
	
	@Override
	public void reset() {
		this.text = defaultText;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDefaultText() {
		return defaultText;
	}
}
