package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventGamma extends Event {
	
	private float gamma;
	
	public EventGamma(float gamma) {
		this.gamma = gamma;
	}

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
	}
}
