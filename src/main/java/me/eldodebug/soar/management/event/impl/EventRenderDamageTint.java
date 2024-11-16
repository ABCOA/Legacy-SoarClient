package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventRenderDamageTint extends Event {

	private float partialTicks;
	
	public EventRenderDamageTint(float partialTicks) {
		this.partialTicks = partialTicks;
	}

	public float getPartialTicks() {
		return partialTicks;
	}
}
