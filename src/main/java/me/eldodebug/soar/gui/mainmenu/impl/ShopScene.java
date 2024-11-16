package me.eldodebug.soar.gui.mainmenu.impl;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import net.minecraft.client.gui.ScaledResolution;

public class ShopScene extends MainMenuScene {

	private ArrayList<TranslateText> premiumFeatures = new ArrayList<TranslateText>();
	private TranslateText premiumInfo;
	
	public ShopScene(GuiSoarMainMenu parent) {
		super(parent);
		
		premiumFeatures.add(TranslateText.SPECIAL_BADGE);
		premiumFeatures.add(TranslateText.SPECIAL_CAPE);
		premiumFeatures.add(TranslateText.CUSTOM_CAPE);
		premiumInfo = TranslateText.PURCHASE;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg));
	}
	
	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Soar instance, NanoVGManager nvg) {
		
		int acWidth = 220;
		int acHeight = 190;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		
		int offsetY = 0;
		
		nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
		nvg.drawCenteredText(TranslateText.PRICING_PLANS.getText(), acX + (acWidth / 2), acY + 12, Color.WHITE, 14, Fonts.MEDIUM);
		nvg.drawCenteredText(TranslateText.PRICING_PLANS_DESCRIPTION.getText(), acX + (acWidth / 2), acY + 30, Color.WHITE, 9, Fonts.REGULAR);
		nvg.drawRoundedRect(acX + 20, acY + 50, 82, 128, 6, this.getBackgroundColor());
		nvg.drawRoundedRect(acX + acWidth - (82) - 20, acY + 50, 82, 128, 6, this.getBackgroundColor());
		
		nvg.drawCenteredText(TranslateText.PREMIUM.getText(), acX + 20 + (82 / 2), acY + 58, Color.WHITE, 12, Fonts.MEDIUM);
		nvg.drawCenteredText("15$ / " + TranslateText.LIFETIME.getText(), acX + 20 + (82 / 2), acY + 71, Color.WHITE, 8, Fonts.REGULAR);
		nvg.drawRect(acX + 20, acY + 80, 82, 1, Color.WHITE);
		
		nvg.drawRoundedRect(acX + 25, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
		nvg.drawRoundedRect(acX + 25, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
		nvg.drawCenteredText(premiumInfo.getText(), acX + 25 + (72 / 2), acY + 159, Color.WHITE, 10, Fonts.MEDIUM);
		
		for(TranslateText t : premiumFeatures) {
			
			nvg.drawText(Icon.CHECK_CIRCLE, acX + 25, acY + 87 + offsetY, Color.WHITE, 9, Fonts.ICON);
			nvg.drawText(t.getText(), acX + 36, acY + 88 + offsetY, Color.WHITE, 8, Fonts.REGULAR);
			
			offsetY+=12;
		}
		
		nvg.drawCenteredText(TranslateText.SOON.getText(), acX + acWidth - (82) - 20 + (82 / 2), acY + 58, Color.WHITE, 12, Fonts.MEDIUM);
		nvg.drawCenteredText("??$ / " + TranslateText.MONTH.getText(), acX + acWidth - (82) - 20 + (82 / 2), acY + 71, Color.WHITE, 8, Fonts.REGULAR);
		nvg.drawRect(acX + acWidth - (82) - 20, acY + 80, 82, 1, Color.WHITE);
		
		nvg.drawRoundedRect(acX + acWidth - (82) - 20 + 5, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
		nvg.drawRoundedRect(acX + acWidth - (82) - 20 + 5, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
		nvg.drawCenteredText(TranslateText.SOON.getText(), acX + acWidth - (82) - 15 + (72 / 2), acY + 159, Color.WHITE, 10, Fonts.MEDIUM);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(keyCode == Keyboard.KEY_ESCAPE) {
			this.setCurrentScene(this.getSceneByClass(MainScene.class));
		}
	}
}
