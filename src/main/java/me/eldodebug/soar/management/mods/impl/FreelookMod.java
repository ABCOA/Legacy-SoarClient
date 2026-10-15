package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.Priority;
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
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class FreelookMod extends Mod {

	private boolean active;
	private float yaw;
	private float pitch;
	private int previousPerspective;
	private boolean toggled;
	private boolean distancePreview;
	private boolean wasActiveBeforePreview;
	
	private NumberSetting distanceSetting = new NumberSetting(TranslateText.CAMERA_DISTANCE, this, 4, 0, 15, false) {
		@Override
		public void setValue(double value) {
			// Keep distances saved with the previous range within the current slider limits.
			super.setValue(Math.max(getMinValue(), Math.min(getMaxValue(), value)));
		}
	};
	private BooleanSetting invertYawSetting = new BooleanSetting(TranslateText.INVERT_YAW, this, false);
	private BooleanSetting invertPitchSetting = new BooleanSetting(TranslateText.INVERT_PITCH, this, false);
	private ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.KEYDOWN, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.TOGGLE), new Option(TranslateText.KEYDOWN))));
	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_C);
	
	public FreelookMod() {
		super(TranslateText.FREELOOK, TranslateText.FREELOOK_DESCRIPTION, ModCategory.PLAYER);
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		if(mc.theWorld == null || mc.thePlayer == null) {
			toggled = false;
			stop();
			return;
		}
		if(distancePreview) {
			start();
			return;
		}
		
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
		if(distancePreview) return;
		
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
	
	@EventTarget(Priority.FIFTH)
	public void onCameraRotation(EventCameraRotation event) {
		if(active) {
			event.setYaw(yaw);
			event.setPitch(pitch);
			event.setThirdPersonDistance(MathHelper.clamp_float(distanceSetting.getValueFloat(), 0, 15));
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
			Entity renderView = mc.getRenderViewEntity();
			if(mc.theWorld == null || mc.thePlayer == null || renderView == null) return;
			active = true;
			previousPerspective = mc.gameSettings.thirdPersonView;
			mc.gameSettings.thirdPersonView = 3;
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

	public boolean beginDistancePreview() {
		if(mc.theWorld == null || mc.thePlayer == null || mc.getRenderViewEntity() == null) return false;
		if(distancePreview) return true;
		wasActiveBeforePreview = active;
		distancePreview = true;
		// Preview also works with the module disabled, without changing its saved preference.
		if(!isToggled()) Soar.getInstance().getEventManager().register(this);
		start();
		return true;
	}

	public void endDistancePreview() {
		if(!distancePreview) return;
		distancePreview = false;
		if(!wasActiveBeforePreview || mc.theWorld == null || mc.thePlayer == null) stop();
		wasActiveBeforePreview = false;
		if(!isToggled()) Soar.getInstance().getEventManager().unregister(this);
	}

	public boolean isDistancePreview() { return distancePreview; }
	public NumberSetting getDistanceSetting() { return distanceSetting; }

	@Override
	public void onDisable() {
		distancePreview = false;
		wasActiveBeforePreview = false;
		toggled = false;
		stop();
		super.onDisable();
	}
}
