package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.music.Music;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.music.MusicType;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.ui.comp.impl.CompSlider;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.SearchUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

public class MusicCategory extends Category {

	private CompSlider volumeSlider;
	
	private MusicType currentType;
	
	private boolean openDownloader;
	private SimpleAnimation downloaderAnimation = new SimpleAnimation();
	private CompTextBox textBox = new CompTextBox();
	
	public MusicCategory(GuiModMenu parent) {
		super(parent, TranslateText.MUSIC, Icon.MUSIC, true);
	}

	@Override
	public void initGui() {
		currentType = MusicType.ALL;
		openDownloader = false;
		
		if(volumeSlider == null) {
			volumeSlider = new CompSlider(GlobalSettingsMod.getInstance().getVolumeSetting());
		}
	}
	
	@Override
	public void initCategory() {
		scroll.resetAll();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		AccentColor accentColor = colorManager.getCurrentColor();
		MusicManager musicManager = instance.getMusicManager();
		Music currentMusic = musicManager.getCurrentMusic();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		
		nvg.save();
		nvg.translate(0, scroll.getValue());
		
		for(MusicType t : MusicType.values()) {
			
			float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
			boolean isCurrentCategory = t.equals(currentType);
			
			t.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);
			
			Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
			Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (t.getBackgroundAnimation().getValue() * 255));
			Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (t.getBackgroundAnimation().getValue() * 255));
			Color textColor = t.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);
			
			nvg.drawText(t.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);
			
			offsetX+=textWidth + 28;
		}
		
		offsetY = offsetY + 23;
		
		for(Music m : musicManager.getMusics()) {
			
			if(filter(m)) {
				continue;
			}
			
			nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, 8, palette.getBackgroundColor(ColorType.DARK));
			
			if(m.getIcon() == null) {
				nvg.drawRoundedImage(new ResourceLocation("soar/music.png"), this.getX() + 21, this.getY() + offsetY + 6, 34, 34, 6);
			}else {
				nvg.drawRoundedImage(m.getIcon(), this.getX() + 21, this.getY() + offsetY + 6, 34, 34, 6);
			}
			
			m.getFavoriteAnimation().setAnimation(m.getType().equals(MusicType.FAVORITE) ? 1.0F : 0.0F, 16);
			
			nvg.drawText(nvg.getLimitText(m.getName(), 11, Fonts.MEDIUM, 280), this.getX() + 63, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
			nvg.drawText(Icon.STAR, this.getX() + this.getWidth() - 56.5F, this.getY() + offsetY + 16, palette.getMaterialYellow(), 13, Fonts.ICON);
			nvg.drawText(Icon.STAR_FILL, this.getX() + this.getWidth() - 56.5F, this.getY() + offsetY + 16, palette.getMaterialYellow((int) (m.getFavoriteAnimation().getValue() * 255)), 13, Fonts.ICON);
			nvg.drawText(Icon.TRASH, this.getX() + this.getWidth() - 39, this.getY() + offsetY + 16, palette.getMaterialRed(), 13, Fonts.ICON);
			
			index++;
			offsetY+=56;
		}
		
		nvg.restore();
		
		downloaderAnimation.setAnimation(openDownloader ? 1.0F : 0.0F, 16);
		
		nvg.save();
		nvg.translate(0, 60 - (downloaderAnimation.getValue() * 60));
		
		nvg.drawRoundedRect(this.getX() + this.getWidth() - 175, this.getY() + this.getHeight() - 86, 165, 30, 6, palette.getBackgroundColor(ColorType.DARK));
		textBox.setPosition(this.getX() + this.getWidth() - 169, this.getY() + this.getHeight() - 80, 129, 18);
		textBox.draw(mouseX, mouseY, partialTicks);
		
		nvg.drawRoundedRect(this.getX() + this.getWidth() - 34, this.getY() + this.getHeight() - 80, 18, 18, 4, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(Icon.DOWNLOAD, this.getX() + this.getWidth() - 30.5F, this.getY() + this.getHeight() - 76.5F, palette.getFontColor(ColorType.DARK), 11, Fonts.ICON);
		
		nvg.restore();
		
		nvg.drawRoundedRectVarying(this.getX(), this.getY() + this.getHeight() - 46F, this.getWidth(), 46, 0, 0, 0, 12, palette.getBackgroundColor(ColorType.DARK));
		
		if(currentMusic == null || currentMusic.getIcon() == null) {
			nvg.drawRoundedImage(new ResourceLocation("soar/music.png"), this.getX() + 15, this.getY() + this.getHeight() - 40F, 34, 34, 6);
		}else {
			nvg.drawRoundedImage(musicManager.getCurrentMusic().getIcon(), this.getX() + 15, this.getY() + this.getHeight() - 40F, 34, 34, 6);
		}
		
		nvg.drawText(currentMusic == null ? TranslateText.NOTHING_IS_PLAYING.getText() : nvg.getLimitText(currentMusic.getName(), 9, Fonts.MEDIUM, 290), this.getX() + 56, this.getY() + this.getHeight() - 37,
				palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
		
		nvg.drawText(currentMusic == null || !musicManager.isPlaying() ? Icon.PLAY : Icon.PAUSE, this.getX() + (this.getWidth() / 2) - 8, this.getY() + this.getHeight() - 21.5F, 
				palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		
		nvg.drawText(Icon.BACK, this.getX() + (this.getWidth() / 2) - 33, this.getY() + this.getHeight() - 21.5F, 
				palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		
		nvg.drawText(Icon.FORWARD, this.getX() + (this.getWidth() / 2) + 17, this.getY() + this.getHeight() - 21.5F, 
				palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		
		nvg.drawText("+" + TranslateText.ADD_SONG.getText(), this.getX() + this.getWidth() - (nvg.getTextWidth("+" + TranslateText.ADD_SONG.getText(), 9, Fonts.MEDIUM)) - 10, this.getY() + this.getHeight() - 37,
				palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
		
		volumeSlider.setX(this.getX() + this.getWidth() - 62 - 10);
		volumeSlider.setY(this.getY() + this.getHeight() - 20);
		volumeSlider.setWidth(62);
		volumeSlider.setHeight(4.5);
		volumeSlider.setCircle(false);
		volumeSlider.setShowValue(false);
		volumeSlider.draw(mouseX, mouseY, partialTicks);
		musicManager.setVolume();
		
		float volume = volumeSlider.getSetting().getValueFloat();
		
		String volumeIcon = Icon.VOLUME;
		
		if(volume == 0) {
			volumeIcon = Icon.VOLUME_X;
		}
		if(volume > 0.1F) {
			volumeIcon = Icon.VOLUME;
		}
		if(volume > 0.4F) {
			volumeIcon = Icon.VOLUME_1;
		}
		if(volume > 0.8F) {
			volumeIcon = Icon.VOLUME_2;
		}
		
		nvg.drawText(volumeIcon, this.getX() + this.getWidth() - 94, this.getY() + this.getHeight() - 26,
				palette.getFontColor(ColorType.NORMAL), 16, Fonts.ICON);
		
		scroll.setMaxScroll((index - (index > 3 ? 3.91F : index)) * 56);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		MusicManager musicManager = instance.getMusicManager();
		
		int offsetX = 0;
		float offsetY = 13 + scroll.getValue();
		
		if(openDownloader) {
			
			textBox.mouseClicked(mouseX, mouseY, mouseButton);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 34, this.getY() + this.getHeight() - 80, 18, 18) && mouseButton == 0) {
				
				openDownloader = false;
				
				Multithreading.runAsync(() -> {
					
					instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.ADDED_MUSIC_QUEUE, NotificationType.INFO);
					
					if(musicManager.getYtdlp().download(textBox.getText())) {
						instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.MUSIC_DOWNLOAD_COMPLETE, NotificationType.SUCCESS);
						musicManager.load();
					} else {
						instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.MUSIC_DOWNLOAD_FAILED, NotificationType.ERROR);
					}
				});
				
				return;
			}
			
			if(!MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 175, this.getY() + this.getHeight() - 86, 165, 30)) {
				openDownloader = false;
				return;
			}
		}
		
		for(MusicType t : MusicType.values()) {
			
			float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
					currentType = t;
				}
			}
			
			offsetX+=textWidth + 28;
		}
	
		offsetY = offsetY + 23;
		
		for(Music m : musicManager.getMusics()) {
			
			if(filter(m)) {
				continue;
			}
			
			if(mouseButton == 0 && !openDownloader && MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight() - 46)) {
				
				boolean inside = MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46);
				boolean favorite = MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 59, this.getY() + offsetY + 14, 18, 18);
				boolean trash = MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 41, this.getY() + offsetY + 14, 18, 18);
				
				if(inside && !favorite && !trash) {
					musicManager.stop();
					musicManager.setCurrentMusic(m);
					musicManager.play();
				}
				
				if(favorite) {
					if(m.getType().equals(MusicType.ALL)) {
						m.setType(MusicType.FAVORITE);
					}else {
						m.setType(MusicType.ALL);
					}
					musicManager.saveData();
				}
				
				if(trash) {
					musicManager.delete(m);
				}
			}
			
			offsetY+=56;
		}
		
		if(mouseButton == 0) {
			
			float textWidth = nvg.getTextWidth("+" + TranslateText.ADD_SONG.getText(), 9, Fonts.MEDIUM);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2) - 9, this.getY() + this.getHeight() - 21.5F, 17, 17)) {
				musicManager.switchPlayBack();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2) - 34, this.getY() + this.getHeight() - 22.5F, 18, 18)) {
				musicManager.back();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2) + 16, this.getY() + this.getHeight() - 22.5F, 18, 18)) {
				musicManager.next();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - textWidth - 10, this.getY() + this.getHeight() - 39, textWidth, 12)) {
				if(openDownloader) {
					openDownloader = false;
				}else {
					textBox.setText("");
					openDownloader = true;
				}
			}
		}
		
		volumeSlider.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		volumeSlider.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if(openDownloader) {
			textBox.keyTyped(typedChar, keyCode);
		}
	}
	
	private boolean filter(Music m) {
		
		if(!currentType.equals(MusicType.ALL) && !m.getType().equals(currentType)) {
			return true;
		}
		
		
		if(!this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(m.getName(), this.getSearchBox().getText())) {
			return true;
		}
		
		return false;
	}
}
