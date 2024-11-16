package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventRenderSelectedItem extends Event {

	private int color;
	
	public EventRenderSelectedItem(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
}