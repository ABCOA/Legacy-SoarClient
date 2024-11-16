package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventScrollMouse extends Event {

	private int amount;
	
	public EventScrollMouse(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}
}