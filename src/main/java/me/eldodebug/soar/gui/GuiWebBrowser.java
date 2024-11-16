package me.eldodebug.soar.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.mcef.McefBrowser;
import me.eldodebug.soar.ui.comp.impl.field.CompSearchBox;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.network.HttpUtils;
import net.minecraft.client.gui.GuiScreen;

public class GuiWebBrowser extends GuiScreen {

	private int offsetY;
	private McefBrowser browser;
	private CompSearchBox searchBox;
	
	public GuiWebBrowser(McefBrowser browser) {
		this.browser = browser;
	}
	
    @Override
    public void initGui() {
    	
    	offsetY = 24;
    	
        if(browser != null) {
        	resizeBrowser();
        }
        
        searchBox = new CompSearchBox();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    	
    	NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
    	
    	nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, partialTicks));
    	
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawNanoVG(int mouseX, int mouseY, float partialTicks) {
    	
    	Soar instance = Soar.getInstance();
    	NanoVGManager nvg = instance.getNanoVGManager();
    	ColorManager colorManager = instance.getColorManager();
    	ColorPalette palette = colorManager.getPalette();
    	
    	nvg.drawRect(0, 0, width, height, palette.getBackgroundColor(ColorType.NORMAL));
    	
    	if(browser != null) {
    		
        	nvg.drawImage(browser.getTexture(), 0, offsetY, width, height - offsetY);
        	
        	nvg.drawRoundedRect(6, 4, 16, 16, 8, palette.getBackgroundColor(ColorType.DARK));
        	nvg.drawRoundedRect(26, 4, 16, 16, 8, palette.getBackgroundColor(ColorType.DARK));
        	nvg.drawRoundedRect(46, 4, 16, 16, 8, palette.getBackgroundColor(ColorType.DARK));
        	nvg.drawRoundedRect(width - 6 - 16, 4, 16, 16, 8, palette.getBackgroundColor(ColorType.DARK));
        	
        	nvg.drawText(Icon.ARROW_LEFT, 8.5F, 6.5F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.ICON);
        	nvg.drawText(Icon.ARROW_RIGHT, 28.5F, 6.5F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.ICON);
        	nvg.drawText(Icon.REFRESH, 48.5F, 6.5F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.ICON);
        	nvg.drawText(Icon.X, width - 19.5F, 6.5F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.ICON);
        	
        	if(!searchBox.isFocused()) {
            	searchBox.setText(browser.getURL());
        	}
        	
        	searchBox.setPosition(68.5F, 4, width - 97, 16);
        	searchBox.draw(offsetY, offsetY, offsetY);
    	}
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    	
    	if(browser != null && mouseButton == 0) {
    		
    		if(MouseUtils.isInside(mouseX, mouseY, 6, 4, 16, 16) && browser.canGoBack()) {
    			browser.goBack();
    		}
    		
    		if(MouseUtils.isInside(mouseX, mouseY, 26, 4, 16, 16) && browser.canGoForward()) {
    			browser.goForward();
    		}
    		
    		if(MouseUtils.isInside(mouseX, mouseY, 46, 4, 16, 16)) {
    			browser.reload();
    		}
    		
    		if(MouseUtils.isInside(mouseX, mouseY, width - 6 - 16, 4, 16, 16)) {
    			mc.displayGuiScreen(null);
    		}
    		
    		searchBox.mouseClicked(mouseX, mouseY, mouseButton);
    	}
    	
    	super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
    	
    	if(searchBox.isFocused() && keyCode == Keyboard.KEY_RETURN) {
    		searchBox.setFocused(false);
    		browser.loadURL(HttpUtils.punycode(searchBox.getText()));
    	}
    	
    	searchBox.keyTyped(typedChar, keyCode);
    	
    	super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void handleInput() {
    	
    	while(Keyboard.next()) {
    		
    		if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
    			mc.displayGuiScreen(null);
    			return;
    		}
    		
    		boolean pressed = Keyboard.getEventKeyState();
    		char key = Keyboard.getEventCharacter();
    		int num = Keyboard.getEventKey();
    		
    		if(browser != null) {
    			
    			if(pressed) {
    				browser.sendKeyPressed(num, key, 0);
    			} else {
    				browser.sendKeyReleased(num, key, 0);
    			}
    			
    			if(key != 0) {
     				browser.sendKeyTyped(key, 0);
    			}
    		}
    		
    		if(pressed) {
    			try {
					keyTyped(key, num);
				} catch (IOException e) {}
    		}
    	}
    	
    	while(Mouse.next()) {
    		
    		int button = Mouse.getEventButton();
    		boolean pressed = Mouse.getEventButtonState();
    		int sx = Mouse.getEventX();
    		int sy = Mouse.getEventY();
    		int wheel = Mouse.getEventDWheel();
    		
    		if(browser != null) {
    			
    			int y = mc.displayHeight - sy - scaleY(offsetY);
    			
    			if(wheel != 0) {
    				browser.sendMouseWheel(sx, y, 0, 1, wheel);
    			} else if(button == -1) {
    				browser.sendMouseMove(sx, y, 0, y < 0);
    			} else {
    				browser.sendMouseButton(sx, y, 0, button + 1, pressed, 1);
    			}
    		}
    		
    		if(pressed) {
    			
                int x = sx * width / mc.displayWidth;
                int y = height - (sy * height / mc.displayHeight) - 1;

                try {
                    mouseClicked(x, y, button);
                } catch(Exception e) {}
    		}
    	}
    }
    
    @Override
    public boolean doesGuiPauseGame() {
    	return false;
    }
    
    @Override
    public void onGuiClosed() {
    	browser.resize(1920, 1080);
    }
    
    private void resizeBrowser() {
    	if(width > 100 && height > 100) {
    		browser.resize(mc.displayWidth, mc.displayHeight - scaleY(offsetY));
    	}
    }
    
    public int scaleY(int y) {
    	
        double sy = ((double) y) / ((double) height) * ((double) mc.displayHeight);
        
        return (int) sy;
    }
}
