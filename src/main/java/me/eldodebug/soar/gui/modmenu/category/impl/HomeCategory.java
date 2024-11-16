package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.net.URL;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.account.AccountManager;
import me.eldodebug.soar.management.changelog.Changelog;
import me.eldodebug.soar.management.changelog.ChangelogManager;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.music.Music;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

public class HomeCategory extends Category {

	public HomeCategory(GuiModMenu parent) {
		super(parent, TranslateText.HOME, Icon.HOME, false);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		AccountManager accountManager = instance.getAccountManager();
		MusicManager musicManager = instance.getMusicManager();
		AccentColor currentColor = colorManager.getCurrentColor();
		ChangelogManager changelogManager = instance.getChangelogManager();
		
		// Welcome
		
		File headFile = new File(instance.getFileManager().getCacheDir(), "head/" + accountManager.getCurrentAccount().getName() + ".png");
		
		nvg.drawRoundedRect(this.getX() + 15, this.getY() + 15, 202, 72, 6, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawText(TranslateText.WELCOME_TO_SOAR.getText(), this.getX() + 24, this.getY() + 24, palette.getFontColor(ColorType.DARK), 14, Fonts.MEDIUM);
		
		if(headFile == null || !headFile.exists()) {
			nvg.drawPlayerHead(new ResourceLocation("textures/entity/steve.png"), this.getX() + 24, this.getY() + 43, 36, 36, 6);
		}else {
			nvg.drawRoundedImage(headFile, this.getX() + 24, this.getY() + 43, 36, 36, 6);
		}
		
		nvg.drawText(accountManager.getCurrentAccount().getName(), this.getX() + 66, this.getY() + 46, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
		
		
		// Music
		
		Music currentMusic = musicManager.getCurrentMusic();
		String title = TranslateText.NOTHING_IS_PLAYING.getText();
		
		nvg.drawRoundedRect(this.getX() + 15, this.getY() + 100, 202, 51, 6, palette.getBackgroundColor(ColorType.DARK));
		
		if(currentMusic == null || currentMusic.getIcon() == null) {
			nvg.drawRoundedImage(new ResourceLocation("soar/music.png"), this.getX() + 22, this.getY() + 107, 37, 37, 6);
		}else {
			nvg.drawRoundedImage(musicManager.getCurrentMusic().getIcon(), this.getX() + 22, this.getY() + 107, 37, 37, 6);
			title = currentMusic.getName();
		}
		
		nvg.drawText(nvg.getLimitText(title, 9.6F, Fonts.MEDIUM, 138), this.getX() + 66, this.getY() + 109.5F, palette.getFontColor(ColorType.DARK), 9.6F, Fonts.MEDIUM);
		nvg.drawRoundedRect(this.getX() + 66, this.getY() + 122, 144, 3F, 1.6F, palette.getBackgroundColor(ColorType.NORMAL));
		
		if(currentMusic != null) {
			
			float current = musicManager.getCurrentTime();
			float end = musicManager.getEndTime();
			
			nvg.drawGradientRoundedRect(this.getX() + 68, this.getY() + 122, (current / end) * 144, 3F, 1.6F, currentColor.getColor1(), currentColor.getColor2());
		}
		
		nvg.drawText(currentMusic == null || !musicManager.isPlaying() ? Icon.PLAY : Icon.PAUSE, 
				this.getX() + 68 + (144 / 2) - 10, this.getY() + 128.5F, palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		nvg.drawText(Icon.BACK, this.getX() + 68 + (144 / 2) - 10 - 24, this.getY() + 128.5F, palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		nvg.drawText(Icon.FORWARD, this.getX() + 68 + (144 / 2) - 10 + 24, this.getY() + 128.5F, palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		
		// Changelog
		
		int offsetY = 0;
		
		nvg.drawRoundedRect(this.getX() + 230, this.getY() + 15, 174, 136, 8, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawRect(this.getX() + 230, this.getY() + 15 + 24, 174, 1, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(TranslateText.CHANGELOG.getText(), this.getX() + 230 + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 13.5F, Fonts.MEDIUM);
		
		for(Changelog c : changelogManager.getChangelogs()) {
			
			nvg.drawRoundedRect(this.getX() + 230 + 8, this.getY() + 45 + offsetY, 48, 13, 2.5F, c.getType().getColor());
			nvg.drawCenteredText(c.getType().getText(), this.getX() + 230 + 8 + (48 / 2), this.getY() + 48.5F + offsetY, Color.WHITE, 8, Fonts.MEDIUM);
			nvg.drawText(c.getText(), this.getX() + 230 + 61, this.getY() + 48F + offsetY, palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
			
			offsetY+=16;
		}
		
		// Discord
		
		nvg.drawGradientRoundedRect(this.getX() + 15, this.getY() + 164, 389, 70, 8, currentColor.getColor1(), currentColor.getColor2());
		nvg.drawText(TranslateText.JOIN_OUR_DISCORD_SERVER.getText(), this.getX() + 15 + 50, this.getY() + 164 + 11, Color.WHITE, 13.5F, Fonts.MEDIUM);
		nvg.drawText(TranslateText.DISCORD_DESCRIPTION.getText(), this.getX() + 15 + 50, this.getY() + 164 + 27, Color.WHITE, 9, Fonts.REGULAR);
		nvg.drawRoundedRect(this.getX() + 15 + 10, this.getY() + 164 + 11, 34, 34, 34 / 2, Color.WHITE);
		nvg.drawText(Icon.DISCORD, this.getX() + 15 + 18.5F, this.getY() + 164 + 20.5F, currentColor.getInterpolateColor(), 17, Fonts.ICON);
		
		nvg.drawRoundedRect(this.getX() + 15 + 50, this.getY() + 164 + 43, 52, 18, 8, Color.WHITE);
		nvg.drawCenteredText(TranslateText.JOIN.getText() + " >", this.getX() + 15 + 50 + (52 / 2), this.getY() + 164 + 48, currentColor.getInterpolateColor(), 9, Fonts.REGULAR);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Soar instance = Soar.getInstance();
		MusicManager musicManager = instance.getMusicManager();
		
		if(mouseButton == 0) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 68 + (144 / 2) - 11, this.getY() + 128.5F, 16, 16)) {
				musicManager.switchPlayBack();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 68 + (144 / 2) - 11 - 23, this.getY() + 128.5F, 16, 16)) {
				musicManager.back();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 68 + (144 / 2) - 11 + 25, this.getY() + 128.5F, 16, 16)) {
				musicManager.next();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + 50, this.getY() + 164 + 43, 52, 18)) {
				try {
					Desktop.getDesktop().browse(new URL("https://discord.gg/soar-client-967307105516281917").toURI());
				} catch (Exception e) {}
			}
		}
	}
}
