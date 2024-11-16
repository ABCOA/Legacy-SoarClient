package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.impl.mechibes.SoundKey;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.RandomUtils;
import me.eldodebug.soar.utils.Sound;
import net.minecraft.util.ResourceLocation;

public class MechvibesMod extends Mod {

	private Sound mouseLeftSound = new Sound();
	private Sound mouseRightSound = new Sound();
	
	private HashMap<Integer, SoundKey> keyMap = new HashMap<Integer, SoundKey>();
	private float tempKeyboardVolume;
	private String tempKeyboardMode;
	
	private float tempMouseVolume;
	
	private boolean mouseLeftPress, mouseRightPress;
	
	private boolean loaded;
	
	private BooleanSetting keyboardSetting = new BooleanSetting(TranslateText.KEYBOARD, this, true);
	private ComboSetting keyTypeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.NK_CREAM, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NK_CREAM), new Option(TranslateText.MX_BLUE),
			new Option(TranslateText.MX_SILVER), new Option(TranslateText.RAZER_GREEN),
			new Option(TranslateText.HYPERX_AQUA), new Option(TranslateText.MX_BLACK), new Option(TranslateText.TOPRE_PURPLE))));
	private NumberSetting keyboardVolumeSetting = new NumberSetting(TranslateText.KEYBOARD_VOLUME, this, 0.5, 0.0, 1.0, false);
	
	private BooleanSetting mouseSetting = new BooleanSetting(TranslateText.MOUSE, this, true);
	private NumberSetting mouseVolumeSetting = new NumberSetting(TranslateText.MOUSE_VOLUME, this, 0.5, 0.0, 1.0, false);
	
	public MechvibesMod() {
		super(TranslateText.MECHVIBES, TranslateText.MECHVIBES_DESCRIPTION, ModCategory.OTHER);
	}
	
	@Override
	public void setup() {
		loaded = false;
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		loadKeyboardSounds(getKeyboardType(keyTypeSetting.getOption()).replace("-", "_"));
		loadMouseSounds();
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		
		if(loaded) {
			
			String mode = getKeyboardType(keyTypeSetting.getOption());
			
			if(tempKeyboardMode != mode) {
				
				tempKeyboardMode = mode;
				
				loadKeyboardSounds(mode.replace("-", "_"));
			}
			
			if(tempKeyboardVolume != keyboardVolumeSetting.getValueFloat()) {
				
				tempKeyboardVolume = keyboardVolumeSetting.getValueFloat();
				
				for(Map.Entry<Integer, SoundKey> map : keyMap.entrySet()) {
					map.getValue().setVolume(tempKeyboardVolume);
				}
			}
			
			if(tempMouseVolume != mouseVolumeSetting.getValueFloat()) {
				
				tempMouseVolume = mouseVolumeSetting.getValueFloat();
				
				mouseLeftSound.setVolume(tempMouseVolume);
				mouseRightSound.setVolume(tempMouseVolume);
			}
			
			for(Map.Entry<Integer, SoundKey> map : keyMap.entrySet()) {
				if(map.getValue().isPressed() && !Keyboard.isKeyDown(map.getKey())) {
					map.getValue().setPressed(false);
				}
			}
			
			if(keyboardSetting.isToggled()) {
				for(int keyCode = 0; keyCode < 256; keyCode++) {
					if(Keyboard.isKeyDown(keyCode)) {
						if(keyMap.get(keyCode) != null) {
							SoundKey key = keyMap.get(keyCode);
							if(!key.isPressed()) {
								key.play();
								key.setPressed(true);
							}
							
							continue;
						}
					}
				}
			}
			
			if(mouseSetting.isToggled()) {
				
				if(Mouse.isButtonDown(0) && !mouseLeftPress) {
					mouseLeftPress = true;
					mouseLeftSound.play();
				}
				
				if(!Mouse.isButtonDown(0) && mouseLeftPress) {
					mouseLeftPress = false;
				}
				
				if(Mouse.isButtonDown(1) && !mouseRightPress) {
					mouseRightPress = true;
					mouseRightSound.play();
				}
				
				if(!Mouse.isButtonDown(1) && mouseRightPress) {
					mouseRightPress = false;
				}
			}
		}
	}
	
	private void loadKeyboardSounds(String type) {
		
		Multithreading.runAsync(()-> {
			for(int keyCode = 0; keyCode < 256; keyCode++) {
				
				if(keyCode == Keyboard.KEY_TAB) {
					keyMap.put(Keyboard.KEY_TAB, new SoundKey(type, "tab"));
					continue;
				}
				
				if(keyCode == 14) {
					keyMap.put(14, new SoundKey(type, "backspace"));
					continue;
				}
				
				if(keyCode == 58) {
					keyMap.put(58, new SoundKey(type, "capslock"));
					continue;
				}
				
				if(keyCode == 28) {
					keyMap.put(28, new SoundKey(type, "enter"));
					continue;
				}
				
				if(keyCode == Keyboard.KEY_SPACE) {
					keyMap.put(Keyboard.KEY_SPACE, new SoundKey(type, "space"));
					continue;
				}
				
				if(keyCode == Keyboard.KEY_LSHIFT) {
					keyMap.put(Keyboard.KEY_LSHIFT, new SoundKey(type, "shift"));
					continue;
				}
				
				if(keyCode == Keyboard.KEY_RSHIFT) {
					keyMap.put(Keyboard.KEY_RSHIFT, new SoundKey(type, "shift"));
					continue;
				}
				
				keyMap.put(keyCode, new SoundKey(type, String.valueOf(RandomUtils.getRandomInt(1, 5))));
			}
		});
	}
	
	private String getKeyboardType(Option option) {
		
		if(option.getTranslate().equals(TranslateText.NK_CREAM)) {
			return "nk-cream";
		}
		
		if(option.getTranslate().equals(TranslateText.MX_BLUE)) {
			return "mx-blue";
		}
		
		if(option.getTranslate().equals(TranslateText.MX_SILVER)) {
			return "mx-silver";
		}
		
		if(option.getTranslate().equals(TranslateText.RAZER_GREEN)) {
			return "razer-green";
		}
		
		if(option.getTranslate().equals(TranslateText.HYPERX_AQUA)) {
			return "hyperX-aqua";
		}
		
		if(option.getTranslate().equals(TranslateText.MX_BLACK)) {
			return "mx-black";
		}
		
		if(option.getTranslate().equals(TranslateText.TOPRE_PURPLE)) {
			return "topre-purple";
		}
		
		return "nk-cream";
	}
	
	private void loadMouseSounds() {
		Multithreading.runAsync(()->{
			try {
				mouseLeftSound.loadClip(new ResourceLocation("soar/mechvibes/mouse.wav"));
				mouseRightSound.loadClip(new ResourceLocation("soar/mechvibes/mouse.wav"));
			} catch (Exception e) {}
			loaded = true;
		});
	}
}
