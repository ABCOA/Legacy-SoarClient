package me.eldodebug.soar.management.security;

import me.eldodebug.soar.Soar;

public class SecurityFeature {

	public SecurityFeature() {
		Soar.getInstance().getEventManager().register(this);
	}
}
