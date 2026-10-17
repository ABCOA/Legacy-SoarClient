package me.eldodebug.soar.management.mods.settings.impl;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.utils.ColorUtils;

public class ColorSetting extends Setting {

	private float hue, saturation, brightness;
	private int alpha;
	private Color defaultColor, color;
	private boolean showAlpha;
	
	public ColorSetting(TranslateText text, Mod parent, Color color, boolean showAlpha) {
		super(text, parent);
		
		this.color = color;
		this.defaultColor = color;
		this.hue = ColorUtils.getHue(color);
		this.saturation = ColorUtils.getSaturation(color);
		this.brightness = ColorUtils.getBrightness(color);
		this.alpha = color.getAlpha();
		this.showAlpha = showAlpha;
		
		Soar.getInstance().getModManager().addSettings(this);
	}
	
	@Override
	public void reset() {
        Object before = getColor();

		this.color = defaultColor;
		this.hue = ColorUtils.getHue(color);
		this.saturation = ColorUtils.getSaturation(color);
		this.brightness = ColorUtils.getBrightness(color);
		this.alpha = color.getAlpha();
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
        Object before = getColor();

		this.color = color;
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public Color getDefaultColor() {
		return defaultColor;
	}

	public float getHue() {
		return hue;
	}

	public void setHue(float hue) {
        Object before = getColor();

		this.hue = hue;
		this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
        Object before = getColor();

		this.saturation = saturation;
		this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
        Object before = getColor();

		this.brightness = brightness;
		this.color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
        Object before = getColor();

		this.alpha = alpha;
		this.color = ColorUtils.applyAlpha(color, alpha);
	
        if (!java.util.Objects.equals(before, getColor())) changed();
    }

	public boolean isShowAlpha() {
		return showAlpha;
	}
}
