package me.eldodebug.soar.management.cape.impl;

import me.eldodebug.soar.management.cape.CapeCategory;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public class Cape {
	
	private String name;
	private ResourceLocation cape;
	private boolean premium;
	private CapeCategory category;
	private final String id;
	
	private SimpleAnimation animation = new SimpleAnimation();
	
	public Cape(String name, ResourceLocation cape, boolean premium, CapeCategory category) {
		this(name, cape, premium, category, cape == null ? "none" : "builtin:" + cape.getResourcePath());
	}

	public Cape(String name, ResourceLocation cape, boolean premium, CapeCategory category, String id) {
		this.name = name;
		this.premium = premium;
		this.category = category;
		this.cape = cape;
		this.id = id;
	}

	public String getId() { return id; }
	protected void setTexture(ResourceLocation texture) { this.cape = texture; }

	public String getName() {
		return name;
	}

	public boolean isPremium() {
		return premium;
	}

	public CapeCategory getCategory() {
		return category;
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}

	public ResourceLocation getCape() {
		return cape;
	}
}
