package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.network.Packet;

public class EventReceivePacket extends Event {

	private Packet<?> packet;
	
	public EventReceivePacket(Packet<?> packet) {
		this.packet = packet;
	}

	public Packet<?> getPacket() {
		return packet;
	}

	public void setPacket(Packet<?> packet) {
		this.packet = packet;
	}
}