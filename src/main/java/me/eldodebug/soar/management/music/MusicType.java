package me.eldodebug.soar.management.music;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.animation.ColorAnimation;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public enum MusicType {
	ALL(0, TranslateText.ALL), FAVORITE(1, TranslateText.FAVORITE);
	
	private ColorAnimation textColorAnimation;
	private SimpleAnimation backgroundAnimation;
	
	private TranslateText nameTranslate;
	private int id;
	
	private MusicType(int id, TranslateText nameTranslate) {
		this.id = id;
		this.nameTranslate = nameTranslate;
		this.backgroundAnimation = new SimpleAnimation();
		this.textColorAnimation = new ColorAnimation();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return nameTranslate.getText();
	}

	public String getKey() {
		return nameTranslate.getKey();
	}

	public ColorAnimation getTextColorAnimation() {
		return textColorAnimation;
	}

	public SimpleAnimation getBackgroundAnimation() {
		return backgroundAnimation;
	}
}
