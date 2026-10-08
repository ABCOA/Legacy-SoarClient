package me.eldodebug.soar.management.badge;

public class Badge {

	private final String id;
	private final String icon;
	private final int color;

	public Badge(String id, String icon, int color) {
		this.id = id;
		this.icon = icon;
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public String getIcon() {
		return icon;
	}

	public int getColor() {
		return color;
	}
}
