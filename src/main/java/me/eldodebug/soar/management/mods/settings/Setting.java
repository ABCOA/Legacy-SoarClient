package me.eldodebug.soar.management.mods.settings;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;

public class Setting {

	private Mod parent;
	private TranslateText nameTranslate;
	
	public Setting(TranslateText nameTranslate, Mod parent) {
		this.nameTranslate = nameTranslate;
		this.parent = parent;
	}

	public void reset() {}
	
	public Mod getParent() {
		return parent;
	}

	public String getName() {
		return nameTranslate.getText();
	}
	
	public String getNameKey() {
		return nameTranslate.getKey();
	}
}
