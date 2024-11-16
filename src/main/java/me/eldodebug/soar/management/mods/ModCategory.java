package me.eldodebug.soar.management.mods;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.animation.ColorAnimation;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public enum ModCategory {
	ALL(TranslateText.ALL), 
	PLAYER(TranslateText.PLAYER), 
	RENDER(TranslateText.RENDER),
	HUD(TranslateText.HUD),
	WORLD(TranslateText.WORLD),
	OTHER(TranslateText.OTHER);
	
	private TranslateText nameTranslate;
	private ColorAnimation textColorAnimation;
	private SimpleAnimation backgroundAnimation;
	
	private ModCategory(TranslateText nameTranslate) {
		this.nameTranslate = nameTranslate;
		this.backgroundAnimation = new SimpleAnimation();
		this.textColorAnimation = new ColorAnimation();
	}

	public String getName() {
		return nameTranslate.getText();
	}

	public SimpleAnimation getBackgroundAnimation() {
		return backgroundAnimation;
	}

	public ColorAnimation getTextColorAnimation() {
		return textColorAnimation;
	}
}
