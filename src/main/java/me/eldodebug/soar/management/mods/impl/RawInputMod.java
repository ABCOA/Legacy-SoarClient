package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;

public class RawInputMod extends Mod {

	private static RawInputMod instance;
	
	private ArrayList<Mouse> mouseList = new ArrayList<Mouse>();
	private MouseThread thread;
	
	private boolean initialised, available;
	private volatile float dx, dy;
	private volatile boolean running;
	
	public RawInputMod() {
		super(TranslateText.RAW_INPUT, TranslateText.RAW_INPUT_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		if(!initialised) {
			
			initialised = true;
			available = true;
			
			try {
				ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
				
				if (env.isSupported()) {
					for (Controller controller : env.getControllers()) {
						if(controller instanceof Mouse) {
							mouseList.add((Mouse) controller);
						}
					}
				}else {
					available = false;
				}
			} catch(Exception e) {
				available = false;
			}
		}
		
		running = true;
		thread = new MouseThread();
		thread.setDaemon(true);
		thread.start();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		running = false;
	}
	
	public static RawInputMod getInstance() {
		return instance;
	}

	public MouseThread getThread() {
		return thread;
	}

	public float getDx() {
		return dx;
	}

	public float getDy() {
		return dy;
	}

	public boolean isAvailable() {
		return available;
	}

	public class MouseThread extends Thread {
		
		@Override
		public void run() {
			while(running) {
				
				available = !mouseList.isEmpty();
				
				for (Mouse mouse : mouseList) {
					
					if (!mouse.poll()) {
						continue;
					}

					float dx = mouse.getX().getPollData();
					float dy = mouse.getY().getPollData();

					if (org.lwjgl.input.Mouse.isGrabbed()) {
						RawInputMod.this.dx += dx;
						RawInputMod.this.dy += dy;
					}
				}
			}
		}
		
		public void reset() {
			RawInputMod.this.dx = 0;
			RawInputMod.this.dy = 0;
		}
	}
}
