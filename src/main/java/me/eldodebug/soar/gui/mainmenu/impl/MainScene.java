package me.eldodebug.soar.gui.mainmenu.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainScene extends MainMenuScene {
	private SimpleAnimation singlePlayerAnimation = new SimpleAnimation();
	private SimpleAnimation multiPlayerAnimation = new SimpleAnimation();
	private SimpleAnimation settingsAnimation = new SimpleAnimation();

	public MainScene(GuiSoarMainMenu parent) {
		super(parent);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
	
		nvg.setupAndDraw(() -> drawNanoVG(nvg, mouseX, mouseY));
	}

	private void drawNanoVG(NanoVGManager nvg, int mouseX, int mouseY) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		boolean isHovered = MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - 27, sr.getScaledHeight() / 2 - 87, 54, 54);

		float yPos = sr.getScaledHeight() / 2 - 22;
		AccentColor currentColor = Soar.getInstance().getColorManager().getCurrentColor();
		Color logoColor = isHovered ? currentColor.getInterpolateColor() : Color.WHITE;
		
		nvg.drawCenteredText(Icon.SOAR, sr.getScaledWidth() / 2, sr.getScaledHeight() / 2 - (nvg.getTextHeight(Icon.SOAR, 54, Fonts.ICON) / 2) - 60, logoColor, 54, Fonts.ICON);

		drawButton(nvg, TranslateText.SINGLEPLAYER.getText(), sr.getScaledWidth() / 2, yPos, mouseX, mouseY, 180, 20,singlePlayerAnimation);
		drawButton(nvg, TranslateText.MULTIPLAYER.getText(), sr.getScaledWidth() / 2, yPos + 26, mouseX, mouseY, 180, 20, multiPlayerAnimation);
		drawButton(nvg, TranslateText.SETTINGS.getText(), sr.getScaledWidth() / 2, yPos + (26 * 2), mouseX, mouseY, 180, 20, settingsAnimation);
	}

	@Override
	public void drawButton(NanoVGManager nvg, String text, float x, float y, int mouseX, int mouseY, int width, int height, SimpleAnimation animation) {
		boolean isHovered = MouseUtils.isInside(mouseX, mouseY, x - 90, y, width, height);
		Color backgroundColor = this.getBackgroundColor();
		AccentColor currentColor = Soar.getInstance().getColorManager().getCurrentColor();
		Color hoverColor = currentColor.getInterpolateColor(0);

		animation.setAnimation(isHovered ? 1.0F : 0.0F, 10);

		if (isHovered) {
			nvg.save();
//			nvg.scissor(x - 90, y, width, height);
//			nvg.drawGradientCircle(mouseX, mouseY, radius, new Color(200, 200, 200), backgroundColor);
			float fillWidth = animation.getValue() * width; // 动态调整填充宽度
			nvg.drawRoundedRect(x - 90, y, fillWidth, height, 4.5F, Color.LIGHT_GRAY); // 从左到右填充
			nvg.drawOutlineRoundedRect(x - 90, y, width, height, 4.5F, 1F, hoverColor);
		}
		nvg.drawRoundedRect(x - 90, y, width, height, 4.5F, backgroundColor);
		nvg.drawCenteredText(text, x, y + 6.5F, isHovered ? new Color(255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200)) : Color.white, 9.5F, Fonts.REGULAR);
		nvg.restore();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		float yPos = sr.getScaledHeight() / 2 - 22;
		
		if(mouseButton == 0) {

			if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - 27, sr.getScaledHeight() / 2 - 87, 54, 54)) {
				try {
					Desktop.getDesktop().browse(new URI("https://blog.abcoc.cn"));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}

			if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (160 / 2), yPos, 160, 20)) {
				mc.displayGuiScreen(new GuiSelectWorld(this.getParent()));
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (180 / 2), yPos + 26, 180, 20)) {
				mc.displayGuiScreen(new GuiMultiplayer(this.getParent()));
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2 - (180 / 2), yPos + (26 * 2), 180, 20)) {
				mc.displayGuiScreen(new GuiOptions(this.getParent(), mc.gameSettings));
			}
		}
	}
}
