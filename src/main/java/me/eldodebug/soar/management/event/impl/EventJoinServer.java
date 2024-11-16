package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventJoinServer extends Event {

	private String ip;
	
	public EventJoinServer(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}
}
