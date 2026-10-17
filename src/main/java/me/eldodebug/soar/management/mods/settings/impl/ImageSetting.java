package me.eldodebug.soar.management.mods.settings.impl;

import java.io.File;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class ImageSetting extends Setting {

	private File image;
	
	public ImageSetting(TranslateText nameTranslate, Mod parent) {
		super(nameTranslate, parent);
		
		this.image = null;
		
		Soar.getInstance().getModManager().addSettings(this);
	}

	@Override
	public void reset() {
        Object before = getImage();

		this.image = null;
	
        if (!java.util.Objects.equals(before, getImage())) changed();
    }

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
        Object before = getImage();

		this.image = image;
	
        if (!java.util.Objects.equals(before, getImage())) changed();
    }
}
