package me.eldodebug.soar.management.mods.impl;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.GuiWebBrowser;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.mcef.Mcef;
import me.eldodebug.soar.mcef.McefBrowser;

public class WebBrowserMod extends HUDMod {

	private static WebBrowserMod instance;
	
	private NumberSetting widthSetting = new NumberSetting(TranslateText.WIDTH, this, 190, 10, 500, true);
	private NumberSetting heightSetting = new NumberSetting(TranslateText.HEIGHT, this, 100, 10, 500, true);
	private NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 1.0F, 0.0F, 1.0F, false);
	
	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_J);
	
	private McefBrowser browser;
	
	public WebBrowserMod() {
		super(TranslateText.WEB_BROWSER, TranslateText.WEB_BROWSER_DESCRIPTION);
		
		instance = this;
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG(nvg));
	}
	
	private void drawNanoVG(NanoVGManager nvg) {
		
		int width = (int) (widthSetting.getValueInt() * this.getScale());
		int height = (int) (heightSetting.getValueInt() * this.getScale());
		
		if(browser != null) {
			nvg.drawShadow(this.getX(), this.getY(), width, height, 6 * this.getScale());
			nvg.drawRoundedImage(browser.getTexture(), this.getX(), this.getY(), width, height, 6 * this.getScale(), alphaSetting.getValueFloat());
		}

		this.setWidth((int) (width / this.getScale()));
		this.setHeight((int) (height / this.getScale()));
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		
		if(event.getKeyCode() == keybindSetting.getKeyCode()) {
			
			if(browser == null) {
				
				if(!Mcef.isInitialized()) {
					Mcef.getClient();
				}
				
		        String url = "https://www.google.com/";
		        
		        boolean transparent = true;
		        browser = Mcef.createBrowser(url, transparent);
		        
		        if(browser == null) {
		        	Soar.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.REQUIRED_FILE_MISSING, NotificationType.ERROR);
		        }
			}
			
			if(browser != null) {
				mc.displayGuiScreen(new GuiWebBrowser(browser));
			}
		}
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		if(browser != null) {
			browser.close();
			browser = null;
		}
	}

	public static WebBrowserMod getInstance() {
		return instance;
	}
}
