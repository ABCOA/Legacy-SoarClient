package me.eldodebug.soar.management.music;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class MusicWaveform {
	
	public static float[] visualizer = new float[150];

	public static SimpleAnimation[] animation = new SimpleAnimation[150];

	static {
	    for (int i = 0; i < 150; i++) {
	        visualizer[i] = 0.0F;
	        animation[i] = new SimpleAnimation();
	    }
	}
}
