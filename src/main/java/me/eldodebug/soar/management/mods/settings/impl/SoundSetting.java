package me.eldodebug.soar.management.mods.settings.impl;

import java.io.File;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class SoundSetting extends Setting {

	private File sound;
	
	public SoundSetting(TranslateText nameTranslate, Mod parent) {
		super(nameTranslate, parent);
		
		this.sound = null;
		
		Soar.getInstance().getModManager().addSettings(this);
	}

	@Override
	public void reset() {
        Object before = getSound();

		this.sound = null;
	
        if (!java.util.Objects.equals(before, getSound())) changed();
    }

	public File getSound() {
		return sound;
	}

	public void setSound(File sound) {
        Object before = getSound();

		this.sound = sound;
	
        if (!java.util.Objects.equals(before, getSound())) changed();
    }
}
