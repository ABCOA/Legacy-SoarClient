package me.eldodebug.soar.management.notification;

import me.eldodebug.soar.management.nanovg.font.Icon;

public enum NotificationType {
	INFO(Icon.INFO), 
	WARNING(Icon.ALERT_TRIANGLE), 
	ERROR(Icon.X_CIRCLE),
	SUCCESS(Icon.CHECK),
	MUSIC(Icon.MUSIC);
	
	private String icon;
	
	private NotificationType(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}
}
