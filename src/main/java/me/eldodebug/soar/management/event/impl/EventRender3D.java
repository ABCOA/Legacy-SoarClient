package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventRender3D extends Event {

	private float partialTicks;
	
	public EventRender3D(float partialTicks) {
		this.partialTicks = partialTicks;
	}

	public float getPartialTicks() {
		return partialTicks;
	}
}