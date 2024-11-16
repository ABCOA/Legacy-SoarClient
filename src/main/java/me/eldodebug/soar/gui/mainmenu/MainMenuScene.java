package me.eldodebug.soar.gui.mainmenu;

import java.awt.Color;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;

public class MainMenuScene {

	public Minecraft mc = Minecraft.getMinecraft();
	private GuiSoarMainMenu parent;
	
	private SimpleAnimation animation = new SimpleAnimation();
	
	public MainMenuScene(GuiSoarMainMenu parent) {
		this.parent = parent;
	}
	
	public void initScene() {}
	
	public void initGui() {}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {}
	
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
	
	public void keyTyped(char typedChar, int keyCode) {}
	
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}
	
	public void handleInput() {}
	
	public void onGuiClosed() {}
	
	public void onSceneClosed() {}
	
	public GuiSoarMainMenu getParent() {
		return parent;
	}

	public void setCurrentScene(MainMenuScene scene) {
		parent.setCurrentScene(scene);
	}
	
	public Color getBackgroundColor() {
		return parent.getBackgroundColor();
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}
	
	public MainMenuScene getSceneByClass(Class<? extends MainMenuScene> clazz) {
		return parent.getSceneByClass(clazz);
	}
}
