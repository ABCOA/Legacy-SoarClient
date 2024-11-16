package me.eldodebug.soar.management.screenshot;

import java.io.File;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class Screenshot {

	private SimpleAnimation selectAnimation = new SimpleAnimation();
	
	private String name;
	private File image;
	
	public Screenshot(File image) {
		this.image = image;
		this.name = image.getName().replace(".png", "");
	}

	public SimpleAnimation getSelectAnimation() {
		return selectAnimation;
	}

	public String getName() {
		return name;
	}

	public File getImage() {
		return image;
	}
}
