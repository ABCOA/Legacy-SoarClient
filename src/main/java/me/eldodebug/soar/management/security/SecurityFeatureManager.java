package me.eldodebug.soar.management.security;

import java.util.ArrayList;

import me.eldodebug.soar.management.security.impl.DemoSecurity;
import me.eldodebug.soar.management.security.impl.ExplosionSecurity;
import me.eldodebug.soar.management.security.impl.Log4jSecurity;
import me.eldodebug.soar.management.security.impl.ParticleSecurity;
import me.eldodebug.soar.management.security.impl.ResourcePackSecurity;
import me.eldodebug.soar.management.security.impl.TeleportSecurity;

public class SecurityFeatureManager {

	private ArrayList<SecurityFeature> features = new ArrayList<SecurityFeature>();
	
	public SecurityFeatureManager() {
		features.add(new DemoSecurity());
		features.add(new ExplosionSecurity());
		features.add(new Log4jSecurity());
		features.add(new ParticleSecurity());
		features.add(new ResourcePackSecurity());
		features.add(new TeleportSecurity());
	}

	public ArrayList<SecurityFeature> getFeatures() {
		return features;
	}
}
