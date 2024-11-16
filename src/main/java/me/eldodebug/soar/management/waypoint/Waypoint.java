package me.eldodebug.soar.management.waypoint;

import java.awt.Color;

public class Waypoint {

	private String world, name;
	private double x, y, z;
	private Color color;
	
	public Waypoint(String world, String name, double x, double y, double z, Color color) {
		this.world = world;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
	}

	public String getWorld() {
		return world;
	}

	public String getName() {
		return name;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public Color getColor() {
		return color;
	}
}
