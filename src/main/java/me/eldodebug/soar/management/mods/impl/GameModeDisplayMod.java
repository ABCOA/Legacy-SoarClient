package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.SimpleHUDMod;
import me.eldodebug.soar.utils.PlayerUtils;

public class GameModeDisplayMod extends SimpleHUDMod {

	public GameModeDisplayMod() {
		super(TranslateText.GAME_MODE_DISPLAY, TranslateText.GAME_MODE_DISPLAY_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		this.draw();
	}
	
	@Override
	public String getText() {
		
		String prefix = "Mode: ";
		
		if(PlayerUtils.isSurvival()) {
			return prefix + "Survival";
		}
		
		if(PlayerUtils.isCreative()) {
			return prefix + "Creative";
		}
		
		if(PlayerUtils.isSpectator()) {
			return prefix + "Spectator";
		}
		
		return prefix + "Error";
	}
}
