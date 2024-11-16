package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventSendChat extends Event {

	private String message;
	
	public EventSendChat(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
