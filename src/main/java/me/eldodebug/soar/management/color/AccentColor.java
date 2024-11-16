package me.eldodebug.soar.management.color;

import java.awt.Color;

import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class AccentColor {

	private SimpleAnimation animation = new SimpleAnimation();
	
	private String name;
	private Color color1, color2;
	
	public AccentColor(String name, Color color1, Color color2) {
		this.name = name;
		this.color1 = color1;
		this.color2 = color2;
	}

	public String getName() {
		return name;
	}

	public Color getColor1() {
		return color1;
	}

	public Color getColor2() {
		return color2;
	}
	
	public Color getInterpolateColor() {
		return ColorUtils.interpolateColors(15, 0, color1, color2);
	}
	
	public Color getInterpolateColor(int index) {
		return ColorUtils.interpolateColors(15, index, color1, color2);
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}
}
