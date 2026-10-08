package me.eldodebug.soar.gui.modmenu.category.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.impl.AppearanceScene;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.impl.LanguageScene;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class SettingCategory extends Category {
	
	private Animation sceneAnimation;
	
	private ArrayList<SettingScene> scenes = new ArrayList<SettingScene>();
	private SettingScene currentScene;
	
	public SettingCategory(GuiModMenu parent) {
		super(parent, TranslateText.SETTINGS, Icon.SETTINGS, false);
		
		scenes.add(new AppearanceScene(this));
		scenes.add(new LanguageScene(this));
	}
	
	@Override
	public void initGui() {
		sceneAnimation = new SmoothStepAnimation(260, 1.0);
		sceneAnimation.setValue(1.0);
		
		for(SettingScene scene : scenes) {
			scene.initGui();
		}
	}

	@Override
	public void initCategory() {
		scroll.resetAll();
		sceneAnimation = new SmoothStepAnimation(260, 1.0);
		sceneAnimation.setValue(1.0);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean interactive = MouseCursor.isInteractive();
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		
		float offsetY = 15;
		
		if(sceneAnimation.isDone(Direction.FORWARDS)) {
			this.setCanClose(true);
			currentScene = null;
		}
		
		nvg.save();
		nvg.translate((float) -(600 - (sceneAnimation.getValue() * 600)), 0);
		
		for(SettingScene scene : scenes) {
			
			nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 40, 8, palette.getBackgroundColor(ColorType.DARK));
			if(currentScene == null && sceneAnimation.isDone(Direction.FORWARDS)) {
				MouseCursor.pointer(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 40);
			}
			nvg.drawText(scene.getName(), this.getX() + 52, this.getY() + offsetY + 15F, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
			nvg.drawText(scene.getIcon(), this.getX() + 26, this.getY() + offsetY + 14, palette.getFontColor(ColorType.DARK), 14, Fonts.ICON);
			nvg.drawText(scene.getDescription(), this.getX() + (nvg.getTextWidth(scene.getName(), 14, Fonts.MEDIUM)) + 56, this.getY() + offsetY + 17, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
			nvg.drawText(">", this.getX() + this.getWidth() - 32, this.getY() + offsetY + 17, palette.getFontColor(ColorType.NORMAL), 10, Fonts.MEDIUM);
			
			offsetY+=50;
		}
		
		nvg.restore();
		
		nvg.save();
		nvg.translate((float) (sceneAnimation.getValue() * 600), 0);
		
		if(currentScene != null) {
			MouseCursor.setInteractive(interactive && sceneAnimation.isDone(Direction.BACKWARDS));
			currentScene.drawScreen(mouseX, mouseY, partialTicks);
		}
		MouseCursor.setInteractive(interactive);
		
		nvg.restore();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		float offsetY = 15;
		
		for(SettingScene scene : scenes) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 40) && mouseButton == 0 && currentScene == null) {
				currentScene = scene;
				this.setCanClose(false);
				sceneAnimation.setDirection(Direction.BACKWARDS);
			}
			
			offsetY+=50;
		}
		
		if(currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
			currentScene.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		if(currentScene != null && mouseButton == 3) {
			sceneAnimation.setDirection(Direction.FORWARDS);
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		
		if(currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
			currentScene.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(currentScene != null && keyCode == Keyboard.KEY_ESCAPE) {
			sceneAnimation.setDirection(Direction.FORWARDS);
		}
	}
	
	public int getSceneX() {
		return getX() + 15;
	}
	
	public int getSceneY() {
		return getY() + 15;
	}
	
	public int getSceneWidth() {
		return getWidth() - 30;
	}
	
	public int getSceneHeight() {
		return getHeight() - 30;
	}
}
