package me.eldodebug.soar.management.profile;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

public enum ProfileIcon {
	COMMAND(0, "command"), CRAFTING_TABLE(1, "crafting_table"), FURNACE(2, "furnace"), GRASS(3, "grass"), 
	HAY(4, "hay"), PUMPKIN(5, "pumpkin"), TNT(6, "tnt");
	
	private SimpleAnimation animation = new SimpleAnimation();
	
	private int id;
	private ResourceLocation icon;
	
	private ProfileIcon(int id, String name) {
		this.id = id;
		this.icon = new ResourceLocation("soar/icons/" + name + ".png");
	}

	public ResourceLocation getIcon() {
		return icon;
	}

	public int getId() {
		return id;
	}
	
	public SimpleAnimation getAnimation() {
		return animation;
	}

	public static ProfileIcon getIconById(int id) {
		
		for(ProfileIcon pi : ProfileIcon.values()) {
			if(pi.getId() == id) {
				return pi;
			}
		}
		
		return ProfileIcon.GRASS;
	}
}
