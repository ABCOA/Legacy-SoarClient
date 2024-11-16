package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;

public class EventCameraRotation extends Event {
	
	private float yaw;
	private float pitch;
	private float roll;
	private float thirdPersonDistance;
	
	public EventCameraRotation(float yaw, float pitch, float roll, float thirdPersonDistance) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
		this.thirdPersonDistance = thirdPersonDistance;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	public float getThirdPersonDistance() {
		return thirdPersonDistance;
	}

	public void setThirdPersonDistance(float thirdPersonDistance) {
		this.thirdPersonDistance = thirdPersonDistance;
	}
}