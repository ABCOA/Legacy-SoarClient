package me.eldodebug.soar.management.color;

import java.awt.Color;

import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public enum Theme {
	DARK(0, "Dark", new Color(19, 19, 20), new Color(34, 35, 39), new Color(255, 255, 255), new Color(235, 235, 235)),
	LIGHT(1, "Light", new Color(254, 254, 254), new Color(238, 238, 238), new Color(54, 54, 54), new Color(107, 117, 129)), 
	DARK_BLUE(2, "Dark Blue", new Color(22, 28, 41), new Color(27, 36, 52), new Color(157, 175, 211), new Color(116, 131, 164)), 
	MIDNIGHT(3, "Midnight", new Color(47, 54, 61), new Color(36, 41, 46), new Color(255, 255, 255), new Color(235, 235, 235)), 
	DARK_PURPLE(4, "Dark Purple", new Color(44, 14, 72), new Color(53, 24, 90), new Color(234, 226, 252), new Color(194, 186, 212)),
	SEA(5, "Sea", new Color(203, 224, 255), new Color(190, 216, 238), new Color(255, 255, 255), new Color(245, 245, 245)),
	SAKURA(6,"Sakura",  new Color(255, 191, 178), new Color(255, 223, 226), new Color(255, 255, 255), new Color(245, 245, 245));
	
	private SimpleAnimation animation = new SimpleAnimation();
	
	private String name;
	private int id;
	private Color darkBackgroundColor, normalBackgroundColor;
	private Color darkFontColor, normalFontColor;
	
	private Theme(int id, String name, Color darkBackgroundColor, Color normalBackgroundColor, 
			Color darkFontColor, Color normalFontColor) {
		this.name = name;
		this.id = id;
		this.darkBackgroundColor = darkBackgroundColor;
		this.darkFontColor = darkFontColor;
		this.normalBackgroundColor = normalBackgroundColor;
		this.normalFontColor = normalFontColor;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	public Color getDarkBackgroundColor() {
		return darkBackgroundColor;
	}

	public Color getNormalBackgroundColor() {
		return normalBackgroundColor;
	}

	public Color getDarkFontColor() {
		return darkFontColor;
	}

	public Color getNormalFontColor() {
		return normalFontColor;
	}

	public Color getDarkBackgroundColor(int alpha) {
		return ColorUtils.applyAlpha(darkBackgroundColor, alpha);
	}

	public Color getNormalBackgroundColor(int alpha) {
		return ColorUtils.applyAlpha(normalBackgroundColor, alpha);
	}

	public Color getDarkFontColor(int alpha) {
		return ColorUtils.applyAlpha(darkFontColor, alpha);
	}

	public Color getNormalFontColor(int alpha) {
		return ColorUtils.applyAlpha(normalFontColor, alpha);
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}

	public static Theme getThemeById(int id) {
		
		for(Theme t : Theme.values()) {
			if(t.getId() == id) {
				return t;
			}
		}
		
		return LIGHT;
	}
}
