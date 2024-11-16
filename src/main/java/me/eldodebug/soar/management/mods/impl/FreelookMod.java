package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventPlayerHeadRotation;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class FreelookMod extends Mod {

	private boolean active;
	private float yaw;
	private float pitch;
	private int previousPerspective;
	private boolean toggled;
	
	private BooleanSetting invertYawSetting = new BooleanSetting(TranslateText.INVERT_YAW, this, false);
	private BooleanSetting invertPitchSetting = new BooleanSetting(TranslateText.INVERT_PITCH, this, false);
	private ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.KEYDOWN, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.TOGGLE), new Option(TranslateText.KEYDOWN))));
	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_V);
	
	public FreelookMod() {
		super(TranslateText.FREELOOK, TranslateText.FREELOOK_DESCRIPTION, ModCategory.PLAYER);
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		
		Option mode = modeSetting.getOption();
		
		if(mode.getTranslate().equals(TranslateText.KEYDOWN)) {
			if(keybindSetting.isKeyDown()) {
				start();
			}
			else {
				stop();
			}
		}
		
		if(mode.getTranslate().equals(TranslateText.TOGGLE)) {
			if(toggled) {
				start();
			}else {
				stop();
			}
		}
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		
		Option mode = modeSetting.getOption();
		
		if(mode.getTranslate().equals(TranslateText.TOGGLE)) {
			if(keybindSetting.isKeyDown() ) {
				toggled = !toggled;
			}
		}
		
		if(event.getKeyCode() == mc.gameSettings.keyBindTogglePerspective.getKeyCode()) {
			toggled = false;
		}
	}
	
	@EventTarget
	public void onCameraRotation(EventCameraRotation event) {
		if(active) {
			event.setYaw(yaw);
			event.setPitch(pitch);
		}
	}
	
	@EventTarget
	public void onPlayerHeadRotation(EventPlayerHeadRotation event) {
		
		if(active) {
			float yaw = event.getYaw();
			float pitch = event.getPitch();
			event.setCancelled(true);
			pitch = -pitch;
			
			if(!invertPitchSetting.isToggled()) {
				pitch = -pitch;
			}
			
			if(invertYawSetting.isToggled()) {
				 yaw = -yaw;
			}
			
			this.yaw += yaw * 0.15F;
			this.pitch = MathHelper.clamp_float(this.pitch + (pitch * 0.15F), -90, 90);
			mc.renderGlobal.setDisplayListEntitiesDirty();
		}
	}
	
	private void start() {
		if(!active) {
			active = true;
			previousPerspective = mc.gameSettings.thirdPersonView;
			mc.gameSettings.thirdPersonView = 3;
			Entity renderView = mc.getRenderViewEntity();
			yaw = renderView.rotationYaw;
			pitch = renderView.rotationPitch;
		}
	}
	
	private void stop() {
		if(active) {
			active = false;
			mc.gameSettings.thirdPersonView = previousPerspective;
			mc.renderGlobal.setDisplayListEntitiesDirty();
		}
	}
}
