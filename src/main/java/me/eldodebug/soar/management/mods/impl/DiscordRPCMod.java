package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.discord.DiscordRPC;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class DiscordRPCMod extends Mod {

	private DiscordRPC discord = new DiscordRPC();
	
	public DiscordRPCMod() {
		super(TranslateText.DISCORD_RPC, TranslateText.DISCORD_RPC_DESCRIPTION, ModCategory.OTHER);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		discord.start();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		if(discord.isStarted()) {
			discord.stop();
		}
	}
}
