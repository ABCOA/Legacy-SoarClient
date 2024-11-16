package me.eldodebug.soar.management.changelog;

import java.awt.Color;

import me.eldodebug.soar.management.language.TranslateText;

public enum ChangelogType {
	ADDED(0, TranslateText.ADDED, new Color(68, 200, 129)),
	FIXED(1, TranslateText.FIXED, new Color(220, 220, 68)),
	REMOVED(2, TranslateText.REMOVED, new Color(220, 68, 68)),
	ERROR(999, TranslateText.ERROR, new Color(220, 68, 68));
	
	private int id;
	private TranslateText translateText;
	private Color color;
	
	private ChangelogType(int id, TranslateText translateText, Color color) {
		this.id = id;
		this.translateText = translateText;
		this.color = color;
	}
	
	public int getId() {
		return id;
	}

	public String getText() {
		return translateText.getText();
	}

	public Color getColor() {
		return color;
	}
	
	public static ChangelogType getTypeById(int id) {
		
		for(ChangelogType type : ChangelogType.values()) {
			if(type.getId() == id) {
				return type;
			}
		}
		
		return ChangelogType.ERROR;
	}
}
