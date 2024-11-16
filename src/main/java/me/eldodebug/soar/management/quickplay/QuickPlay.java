package me.eldodebug.soar.management.quickplay;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;

public class QuickPlay {

	private ArrayList<QuickPlayCommand> commands = new ArrayList<QuickPlayCommand>();
	
	private String name;
	private ResourceLocation icon;
	
	public QuickPlay(String name, ResourceLocation icon) {
		this.name = name;
		this.icon = icon;
		this.addCommands();
	}
	
	public void addCommands() {}

	public String getName() {
		return name;
	}
	
	public ArrayList<QuickPlayCommand> getCommands() {
		return commands;
	}

	public ResourceLocation getIcon() {
		return icon;
	}

	public void setCommands(ArrayList<QuickPlayCommand> commands) {
		this.commands = commands;
	}
}
