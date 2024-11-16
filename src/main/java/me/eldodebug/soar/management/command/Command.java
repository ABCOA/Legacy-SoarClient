package me.eldodebug.soar.management.command;

import net.minecraft.client.Minecraft;

public class Command {

	public Minecraft mc = Minecraft.getMinecraft();
	
	private String prefix;
	
	public Command(String prefix) {
		this.prefix = prefix;
	}

	public void onCommand(String message) {}
	
	public String getPrefix() {
		return prefix;
	}
}
