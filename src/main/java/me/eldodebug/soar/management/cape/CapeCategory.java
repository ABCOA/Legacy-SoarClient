package me.eldodebug.soar.management.cape;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.animation.ColorAnimation;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public enum CapeCategory {
	ALL(TranslateText.ALL), MINECON("Minecon"), FLAG(TranslateText.CAPE_CATEGORY_FLAG), CUSTOM(TranslateText.CUSTOM), ABCOC("ABCOC"), NACHONEKO("NachoNeko");

	private String name;
	private TranslateText nameTranslate;
	private SimpleAnimation backgroundAnimation = new SimpleAnimation();
	private ColorAnimation textColorAnimation = new ColorAnimation();

	private CapeCategory(String name) {
		this.name = name;
	}

	private CapeCategory(TranslateText nameTranslate) {
		this.nameTranslate = nameTranslate;
	}

	public String getName() {
		return nameTranslate != null ? nameTranslate.getText() : name;
	}

	public SimpleAnimation getBackgroundAnimation() {
		return backgroundAnimation;
	}

	public ColorAnimation getTextColorAnimation() {
		return textColorAnimation;
	}
}
