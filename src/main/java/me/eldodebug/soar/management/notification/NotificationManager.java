package me.eldodebug.soar.management.notification;

import java.util.concurrent.LinkedBlockingQueue;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;

public class NotificationManager {

	private LinkedBlockingQueue<Notification> notifications = new LinkedBlockingQueue<Notification>();
	
	public NotificationManager() {
		Soar.getInstance().getEventManager().register(new NotificationHandler(notifications));
	}
	
	public void post(TranslateText title, TranslateText message, NotificationType type) {
		notifications.add(new Notification(title, message, type));
	}
}
