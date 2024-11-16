package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventPlaySound;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;

public class SoundModifierMod extends Mod {

	private NumberSetting noteSetting = new NumberSetting(TranslateText.NOTE, this, 100, 0, 100, true);
	private NumberSetting tntSetting = new NumberSetting(TranslateText.TNT, this, 100, 0, 100, true);
	private NumberSetting portalSetting = new NumberSetting(TranslateText.PORTAL, this, 100, 0, 100, true);
	private NumberSetting stepSetting = new NumberSetting(TranslateText.STEP, this, 100, 0, 100, true);
	private NumberSetting mobsSetting = new NumberSetting(TranslateText.MOBS, this, 100, 0, 100, true);
	private NumberSetting recordsSetting = new NumberSetting(TranslateText.RECORDS, this, 100, 0, 100, true);
	private NumberSetting fireworksSetting = new NumberSetting(TranslateText.FIREWORKS, this, 100, 0, 100, true);
	
	public SoundModifierMod() {
		super(TranslateText.SOUND_MODIFIER, TranslateText.SOUND_MODIFIER_DESCRIPTION, ModCategory.OTHER);
	}

	@EventTarget
	public void onPlaySound(EventPlaySound event) {
		
		if(event.getSoundName().startsWith("fireworks")) {
			event.setVolume(fireworksSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().startsWith("records")) {
			event.setVolume(recordsSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().startsWith("step")) {
			event.setVolume(stepSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().contains("mob")) {
			event.setVolume(mobsSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().startsWith("note")) {
			event.setVolume(noteSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().equals("game.tnt.primed") || event.getSoundName().equals("random.explode") || event.getSoundName().equals("creeper.primed")) {
			event.setVolume(tntSetting.getValueInt() / 100F);
		}
		
		if(event.getSoundName().startsWith("portal")) {
			event.setVolume(portalSetting.getValueInt() / 100F);
		}
	}
}
