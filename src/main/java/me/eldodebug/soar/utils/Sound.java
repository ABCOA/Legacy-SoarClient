package me.eldodebug.soar.utils;

import java.io.BufferedInputStream;
import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class Sound {

	private Minecraft mc = Minecraft.getMinecraft();
	private Clip clip;
	
	public void loadClip(ResourceLocation location) throws Exception {
		clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(mc.getResourceManager().getResource(location).getInputStream())));
	}
	
	public void loadClip(File file) throws Exception {
	    clip = AudioSystem.getClip();
	    clip.open(AudioSystem.getAudioInputStream(file));
	}
	
	public void play() {
		if(clip != null) {
			clip.stop();
			clip.setFramePosition(0);
			clip.start();
		}
	}

	public void setVolume(float volume) {
		
		if(clip == null) {
			return;
		}
		
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

		float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
		gainControl.setValue(dB);
	}
	
	public Clip getClip() {
		return clip;
	}
}
