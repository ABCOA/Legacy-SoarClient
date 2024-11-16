package me.eldodebug.soar.management.profile.mainmenu.impl;

import java.io.File;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class CustomBackground extends Background {

	private SimpleAnimation trashAnimation = new SimpleAnimation();
	private File image;
	
	public CustomBackground(int id, String name, File image) {
		super(id, name);
		this.image = image;
	}

	public File getImage() {
		return image;
	}

	public SimpleAnimation getTrashAnimation() {
		return trashAnimation;
	}
}
