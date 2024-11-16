package me.eldodebug.soar.management.color;

import java.awt.Color;
import java.util.ArrayList;

import me.eldodebug.soar.management.color.palette.ColorPalette;

public class ColorManager {

	private ArrayList<AccentColor> colors = new ArrayList<AccentColor>();
	private ColorPalette palette = new ColorPalette();
	
	private AccentColor currentColor;
	private Theme theme;
	
	public ColorManager() {
		
		theme = Theme.LIGHT;
		
		add("Default", new Color(170, 255, 169), new Color(17, 255, 189));
		add("Evening Sunshine", new Color(185, 43, 39), new Color(21, 101, 192));
		add("Light Orange", new Color(255, 183, 94), new Color(237, 143, 3));
		add("Reef", new Color(0, 210, 255), new Color(58, 123, 213));
		add("Amin", new Color(142, 45, 226), new Color(74, 0, 224));
		add("Magic", new Color(89, 193, 115), new Color(93, 38, 193));
		add("Mango Pulp", new Color(240, 152, 25), new Color(237, 222, 93));
		add("Moon Purple", new Color(78, 84, 200), new Color(143, 148, 251));
		add("Aqualicious", new Color(80, 201, 195), new Color(150, 222, 218));
		add("Stripe", new Color(31, 162, 255), new Color(166, 255, 203));
		add("Shifter", new Color(188, 78, 156), new Color(248, 7, 89));
		add("Quepal", new Color(17, 153, 142), new Color(56, 239, 125));
		add("Orca", new Color(68, 160, 141), new Color(9, 54, 55));
		add("Sublime Vivid", new Color(252, 70, 107), new Color(63, 94, 251));
		add("Moon Asteroid", new Color(15, 32, 39), new Color(44, 83, 100));
		add("Summer Dog", new Color(168, 255, 120), new Color(120, 255, 214));
		add("Pink Flavour", new Color(128, 0, 128), new Color(255, 192, 203));
		add("Sin City Red", new Color(237, 33, 58), new Color(147, 41, 30));
		add("Timber", new Color(252, 0, 255), new Color(0, 219, 222));
		add("Pinot Noir", new Color(75, 108, 183), new Color(24, 40, 72));
		add("Dirty Fog", new Color(185, 147, 214), new Color(140, 166, 219));
		add("Piglet", new Color(238, 156, 167), new Color(255, 221, 225));
		add("Little Leaf", new Color(118, 184, 82), new Color(141, 194, 111));
		add("Nelson", new Color(242, 112, 156), new Color(255, 148, 114));
		add("Turquoise flow", new Color(19, 106, 138), new Color(38, 120, 113));
		add("Purplin", new Color(106, 48, 147), new Color(160, 68, 255));
		add("Martini", new Color(253, 252, 71), new Color(36, 254, 65));
		add("SoundCloud", new Color(254, 140, 0), new Color(248, 54, 0));
		add("Inbox", new Color(69, 127, 202), new Color(86, 145, 200));
		add("Amethyst", new Color(157, 80, 187), new Color(110, 72, 170));
		add("Blush", new Color(178, 69, 146), new Color(241, 95, 121));
		
		currentColor = getColorByName("Default");
	}
	
	private void add(String name, Color color1, Color color2) {
		colors.add(new AccentColor(name, color1, color2));
	}

	public ArrayList<AccentColor> getColors() {
		return colors;
	}
	
	public AccentColor getCurrentColor() {
		return currentColor;
	}

	public void setCurrentColor(AccentColor currentColor) {
		this.currentColor = currentColor;
	}

	public AccentColor getColorByName(String name) {
		
		for(AccentColor c : colors) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		
		return getColorByName("Default");
	}

	public Theme getTheme() {
		return theme;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public ColorPalette getPalette() {
		return palette;
	}
}
