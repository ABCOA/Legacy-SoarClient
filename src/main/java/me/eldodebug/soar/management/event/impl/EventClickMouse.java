package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventClickMouse extends Event {

	private int button;
	
	public EventClickMouse(int button) {
		this.button = button;
	}

	public int getButton() {
		return button;
	}
}
