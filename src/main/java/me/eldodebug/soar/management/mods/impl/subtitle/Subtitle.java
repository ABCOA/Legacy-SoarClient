package me.eldodebug.soar.management.mods.impl.subtitle;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public class Subtitle {
	
    private final String subtitle;
    private long startTime;
    private Vec3 location;
    
    private boolean remove;
    private boolean done;
    
    public SimpleAnimation animation = new SimpleAnimation(0.0F);
    
    public Subtitle(String subtitleIn, Vec3 locationIn) {
        this.subtitle = subtitleIn;
        this.location = locationIn;
        this.startTime = Minecraft.getSystemTime();
        this.remove = false;
        this.done = false;
    }

    public String getString() {
        return this.subtitle;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public Vec3 getLocation() {
        return this.location;
    }

    public void refresh(Vec3 locationIn) {
        this.location = locationIn;
        this.startTime = Minecraft.getSystemTime();
    }

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}