package me.eldodebug.soar.management.music;

import java.io.File;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.file.FileUtils;

public class Music {
	
	private SimpleAnimation favoriteAnimation = new SimpleAnimation();
	
	private String name;
	private File audio;
	private File icon;
	private MusicType type;
	
	public Music(File audio, File icon, MusicType type) {
		this.name = FileUtils.getBaseName(audio);
		this.audio = audio;
		this.icon = icon;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public File getAudio() {
		return audio;
	}

	public File getIcon() {
		return icon;
	}

	public MusicType getType() {
		return type;
	}

	public void setType(MusicType type) {
		this.type = type;
	}

	public SimpleAnimation getFavoriteAnimation() {
		return favoriteAnimation;
	}
}
