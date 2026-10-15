package me.eldodebug.soar.gui.modmenu.category.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.online.CloudConfigUploader;
import me.eldodebug.soar.management.online.CloudFavorites;
import me.eldodebug.soar.management.profile.Profile;
import me.eldodebug.soar.management.profile.ProfileIcon;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.management.profile.ProfileType;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.SearchUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.SmoothStepAnimation;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileCategory extends Category {

	private ProfileType currentType;
	private Animation profileAnimation;
	private boolean openProfile;
	private ProfileIcon currentIcon;
	private CompTextBox nameBox = new CompTextBox();
	private CompTextBox serverIpBox = new CompTextBox();
	private CompTextBox titleBox = new CompTextBox();
	private CompTextBox descriptionBox = new CompTextBox();
	private CompTextBox favoritesBox = new CompTextBox();
	private Profile cloudProfile;
	private boolean cloudFavorites;
	private String removing;

	public ProfileCategory(GuiModMenu parent) {
		super(parent, TranslateText.PROFILE, Icon.EDIT, true);
		titleBox.setMaxStringLength(80);
		descriptionBox.setMaxStringLength(300);
		favoritesBox.setMaxStringLength(128);
	}

	@Override
	public void initGui() {
		currentType = ProfileType.ALL;
		currentIcon = ProfileIcon.COMMAND;
		openProfile = false;
		profileAnimation = new SmoothStepAnimation(260, 1.0);
		profileAnimation.setValue(1.0);
	}
	
	@Override
	public void initCategory() {
		scroll.resetAll();
		openProfile = false;
		profileAnimation = new SmoothStepAnimation(260, 1.0);
		profileAnimation.setValue(1.0);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean interactive = MouseCursor.isInteractive();
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ProfileManager profileManager = instance.getProfileManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		
		profileAnimation.setDirection(openProfile ? Direction.BACKWARDS : Direction.FORWARDS);
		
		if(profileAnimation.isDone(Direction.FORWARDS)) {
			nameBox.setText("");
			serverIpBox.setText("");
			cloudProfile = null;
			cloudFavorites = false;
			removing = null;
			this.setCanClose(true);
		}
		
		// Draw profile scene
		MouseCursor.setInteractive(interactive && !openProfile && profileAnimation.isDone(Direction.FORWARDS));
		nvg.save();
		nvg.translate((float) -(600 - (profileAnimation.getValue() * 600)), 0);
		
		for(ProfileType t : ProfileType.values()) {
			
			float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
			boolean isCurrentCategory = t.equals(currentType);
			MouseCursor.pointer(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16);
			
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
		
		offsetX = 0;
		offsetY = offsetY + 23;
		
		for(Profile p : profileManager.getProfiles()) {
			
			if(filter(p)) {
				continue;
			}
			
			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY, 123, 46, 6, palette.getBackgroundColor(ColorType.DARK));
			MouseCursor.pointer(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY, 123, 46);
			
			if(p.getIcon() != null) {
				nvg.drawRoundedImage(p.getIcon().getIcon(), this.getX() + 15 + offsetX + 6, this.getY() + offsetY + 6, 34, 34, 6);
			}
			
			if(p.getName() != "") {
				nvg.drawText(nvg.getLimitText(p.getName(), 10, Fonts.MEDIUM, 68), this.getX() + 62 + offsetX, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);
			}
			
			if(p.getId() == 999) {
				nvg.drawCenteredText(Icon.PLUS, this.getX() + offsetX + 14.5F + (123 / 2), this.getY() + offsetY + 13, palette.getFontColor(ColorType.DARK), 20, Fonts.ICON);
			}else {
				nvg.drawText(Icon.STAR, this.getX() + 62 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialYellow(), 11, Fonts.ICON);
				
				p.getStarAnimation().setAnimation(p.getType().equals(ProfileType.FAVORITE) ? 1.0F : 0.0F, 16);
				
				nvg.drawText(Icon.STAR_FILL, this.getX() + 62 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialYellow((int) (p.getStarAnimation().getValue() * 255)), 11, Fonts.ICON);
				
				nvg.drawText(Icon.TRASH, this.getX() + 62 + 14 + offsetX, this.getY() + 29 + offsetY, palette.getMaterialRed(), 11, Fonts.ICON);

				nvg.drawText(Icon.EXTERNAL_LINK, this.getX() + 62 + 28 + offsetX, this.getY() + 29 + offsetY, palette.getFontColor(ColorType.DARK), 11, Fonts.ICON);
			}
			
			offsetX+=133;
			
			if(index % 3 == 0) {
				offsetX = 0;
				offsetY+=56;
			}
			
			index++;
		}
		
		nvg.restore();
		
		// Draw profile add scene
		MouseCursor.setInteractive(interactive && openProfile && profileAnimation.isDone(Direction.BACKWARDS));
		if(cloudProfile == null && !cloudFavorites) {
			MouseCursor.pointer(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 231, 100, 21);
			MouseCursor.pointer(mouseX, mouseY, this.getX() + this.getWidth() - 232, this.getY() + this.getHeight() - 231, 100, 21);
		}
		MouseCursor.pointer(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44, 100, 21);
		
		nvg.save();
		nvg.translate((float) (profileAnimation.getValue() * 600), 0);
		
		offsetY = 15;
		offsetX = 0;
		
		nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, this.getHeight() - 30, 10, palette.getBackgroundColor(ColorType.DARK));
		
		if(cloudFavorites) {
			drawFavoritesPanel(mouseX, mouseY, partialTicks);
		} else if(cloudProfile != null) {
			drawCloudPanel(mouseX, mouseY, partialTicks);
		} else {
			nvg.drawRoundedRect(this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 231,
					100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawCenteredText(TranslateText.UPLOAD_PROFILE.getText(), this.getX() + this.getWidth() - 124 + 50, this.getY() + this.getHeight() - 224.5F,
					palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
			nvg.drawRoundedRect(this.getX() + this.getWidth() - 232, this.getY() + this.getHeight() - 231,
					100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawCenteredText(TranslateText.CLOUD_FAVORITES.getText(), this.getX() + this.getWidth() - 232 + 50, this.getY() + this.getHeight() - 224.5F,
					palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
			nvg.drawRect(this.getX() + 15, this.getY() + offsetY + 27, this.getWidth() - 30, 1, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawText(TranslateText.ADD_PROFILE.getText(), this.getX() + 26, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
			nvg.drawText(TranslateText.ICON.getText(), this.getX() + 30, this.getY() + offsetY + 35, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
			for(ProfileIcon icon : ProfileIcon.values()) {
				MouseCursor.pointer(mouseX, mouseY, this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32);
			
				int alpha = (int) (icon.getAnimation().getValue() * 255);
			
				icon.getAnimation().setAnimation(currentIcon.equals(icon) ? 1.0F : 0.0F, 16);
			
				nvg.drawRoundedImage(icon.getIcon(), this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32, 6);
			
				nvg.drawGradientOutlineRoundedRect(this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32, 6, 1.6F * icon.getAnimation().getValue(), ColorUtils.applyAlpha(accentColor.getColor1(), alpha), ColorUtils.applyAlpha(accentColor.getColor2(), alpha));
			
				offsetX+=40;
			}
		
			nvg.drawText(TranslateText.NAME.getText(), this.getX() + 30, this.getY() + offsetY + 96, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
			nameBox.setPosition(this.getX() + 32, this.getY() + 128, 130, 18);
			nameBox.setDefaultText(TranslateText.NAME.getText());
			nameBox.draw(mouseX, mouseY, partialTicks);
		
			nvg.drawText(TranslateText.AUTO_LOAD.getText(), this.getX() + (this.getWidth() / 2), this.getY() + offsetY + 95, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		
			serverIpBox.setPosition(this.getX() + (this.getWidth() / 2) + 2, this.getY() + 128, 130, 18);
			serverIpBox.setDefaultText(TranslateText.SERVER_IP.getText());
			serverIpBox.draw(mouseX, mouseY, partialTicks);
		
			nvg.drawRoundedRect(this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44, 
					100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawCenteredText(TranslateText.CREATE.getText(), this.getX() + this.getWidth() - 124 + 50, this.getY() + this.getHeight() - 37.5F,
					palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
		}

		nvg.restore();
		MouseCursor.setInteractive(interactive);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Soar instance = Soar.getInstance();
		ProfileManager profileManager = instance.getProfileManager();
		NanoVGManager nvg = instance.getNanoVGManager();
		ModManager modManager = instance.getModManager();
		FileManager fileManager = instance.getFileManager();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		
		if(openProfile && cloudFavorites) {
			favoritesClicked(mouseX, mouseY, mouseButton);
		} else if(openProfile && cloudProfile != null) {

			titleBox.mouseClicked(mouseX, mouseY, mouseButton);
			descriptionBox.mouseClicked(mouseX, mouseY, mouseButton);

			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44, 100, 21) && mouseButton == 0) {
				CloudConfigUploader uploader = CloudConfigUploader.getInstance();

				if(!uploader.isBusy()) {
					uploader.upload(cloudProfile.getJsonFile(), titleBox.getText(), descriptionBox.getText());
				}
			}
		} else if(openProfile) {
			
			offsetY = 15;
			offsetX = 0;

			for(ProfileIcon icon : ProfileIcon.values()) {
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 32 + offsetX, this.getY() + offsetY + 53, 32, 32) && mouseButton == 0) {
					currentIcon = icon;
				}
				
				offsetX+=40;
			}
			
			nameBox.mouseClicked(mouseX, mouseY, mouseButton);
			serverIpBox.mouseClicked(mouseX, mouseY, mouseButton);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44,  100, 21) && mouseButton == 0) {
				
				if(!nameBox.getText().isEmpty()) {
					
					String serverIp = "";
					
					if(!serverIpBox.getText().isEmpty()) {
						serverIp = serverIpBox.getText();
					}
					
					profileManager.save(new File(fileManager.getProfileDir(), nameBox.getText() + ".json"), serverIp, ProfileType.ALL, currentIcon);
					profileManager.loadProfiles(false);
					
					openProfile = false;
				}
			}
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 231, 100, 21) && mouseButton == 0) {
				SwingUtilities.invokeLater(() -> {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileNameExtensionFilter("JSON File", "json"));
					int returnValue = fileChooser.showOpenDialog(null);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();
						profileManager.upload(selectedFile);
						openProfile = false;
					}
				});
			}
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 232, this.getY() + this.getHeight() - 231, 100, 21) && mouseButton == 0) {
				CloudFavorites favorites = CloudFavorites.getInstance();
				cloudFavorites = true;
				removing = null;
				favoritesBox.setText(favorites.getQuery());
				favorites.refresh(favorites.getQuery(), 0, false);
			}
		} else {
			
			for(ProfileType t : ProfileType.values()) {
				
				float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
					
					if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
						currentType = t;
					}
				}
				
				offsetX+=textWidth + 28;
			}
			
			offsetX = 0;
			offsetY = offsetY + 23;
			
			for(Profile p : profileManager.getProfiles()) {
				
				if(filter(p)) {
					continue;
				}
				
				if(mouseButton == 0) {
					
					boolean favorite = MouseUtils.isInside(mouseX, mouseY, this.getX() + 61 + offsetX, this.getY() + 28 + offsetY, 13, 13);
					boolean delete = MouseUtils.isInside(mouseX, mouseY, this.getX() + 62 + 13 + offsetX, this.getY() + 28 + offsetY, 13, 13);
					boolean cloud = MouseUtils.isInside(mouseX, mouseY, this.getX() + 62 + 27 + offsetX, this.getY() + 28 + offsetY, 13, 13);
					boolean inside = MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY, 123, 46);
					
					if(inside) {
						
						if(p.getId() == 999 && inside) {
							cloudProfile = null;
							cloudFavorites = false;
							openProfile = true;
							this.setCanClose(false);
						} else if(!favorite && !delete && !cloud) {
							modManager.disableAll();
							profileManager.load(p.getJsonFile());
						}
					}
					
					if(p.getId() != 999) {
						if(favorite) {
							
							if(p.getType().equals(ProfileType.FAVORITE)) {
								p.setType(ProfileType.ALL);
							}else {
								p.setType(ProfileType.FAVORITE);
							}
							
							profileManager.save(p.getJsonFile(), p.getServerIp(), p.getType(), p.getIcon());
						}
						
						if(delete) {
							profileManager.delete(p);
						}

						if(cloud && !delete) {
							cloudProfile = p;
							cloudFavorites = false;
							titleBox.setText(p.getName().length() > 80 ? p.getName().substring(0, 80) : p.getName());
							descriptionBox.setText("");
							CloudConfigUploader.getInstance().reset();
							openProfile = true;
							this.setCanClose(false);
						}
					}
				}
				
				offsetX+=133;
				
				if(index % 3 == 0) {
					offsetX = 0;
					offsetY+=56;
				}
				
				index++;
			}
		}
		
		if(mouseButton == 3) {
			openProfile = false;
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(openProfile) {
			
			if(cloudFavorites) {
				favoritesBox.keyTyped(typedChar, keyCode);

				if(favoritesBox.isFocused() && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) {
					removing = null;
					CloudFavorites.getInstance().refresh(favoritesBox.getText(), 0, false);
				}
			} else if(cloudProfile != null) {
				titleBox.keyTyped(typedChar, keyCode);
				descriptionBox.keyTyped(typedChar, keyCode);
			} else {
				nameBox.keyTyped(typedChar, keyCode);
				serverIpBox.keyTyped(typedChar, keyCode);
			}
			
			if(keyCode == Keyboard.KEY_ESCAPE) {
				openProfile = false;
			}
		}
	}
	
	private void drawCloudPanel(int mouseX, int mouseY, float partialTicks) {

		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		CloudConfigUploader uploader = CloudConfigUploader.getInstance();
		float fieldWidth = this.getWidth() - 64;

		nvg.drawText(TranslateText.CLOUD_UPLOAD.getText(), this.getX() + 26, this.getY() + 24, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		nvg.drawRect(this.getX() + 15, this.getY() + 42, this.getWidth() - 30, 1, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(nvg.getLimitText(String.format(TranslateText.CLOUD_PROFILE.getText(), cloudProfile.getName()), 10, Fonts.REGULAR, fieldWidth), this.getX() + 32, this.getY() + 54, palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);

		nvg.drawText(TranslateText.CLOUD_TITLE.getText(), this.getX() + 30, this.getY() + 74, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		titleBox.setPosition(this.getX() + 32, this.getY() + 92, fieldWidth, 18);
		titleBox.setDefaultText(TranslateText.CLOUD_TITLE.getText());
		titleBox.draw(mouseX, mouseY, partialTicks);

		nvg.drawText(TranslateText.CLOUD_DESCRIPTION.getText(), this.getX() + 30, this.getY() + 120, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		descriptionBox.setPosition(this.getX() + 32, this.getY() + 138, fieldWidth, 18);
		descriptionBox.setDefaultText(TranslateText.CLOUD_DESCRIPTION.getText());
		descriptionBox.draw(mouseX, mouseY, partialTicks);

		CloudConfigUploader.Stage stage = uploader.getStage();
		String status = stage == CloudConfigUploader.Stage.IDLE ? TranslateText.CLOUD_HINT.getText() : uploader.getStatus();
		Color statusColor = stage == CloudConfigUploader.Stage.FAILED ? palette.getMaterialRed() : palette.getFontColor(ColorType.NORMAL);
		float lineY = this.getY() + 168;

		for(String line : wrap(nvg, status, fieldWidth, 5)) {
			nvg.drawText(line, this.getX() + 32, lineY, statusColor, 9, Fonts.REGULAR);
			lineY+=13;
		}

		nvg.drawRoundedRect(this.getX() + this.getWidth() - 124, this.getY() + this.getHeight() - 44,
				100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawCenteredText((uploader.isBusy() ? TranslateText.CLOUD_UPLOADING : TranslateText.CLOUD_UPLOAD).getText(), this.getX() + this.getWidth() - 124 + 50, this.getY() + this.getHeight() - 37.5F,
				palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
	}

	private void drawFavoritesPanel(int mouseX, int mouseY, float partialTicks) {

		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		CloudFavorites favorites = CloudFavorites.getInstance();
		List<CloudFavorites.Entry> entries = favorites.getEntries();
		float fieldWidth = this.getWidth() - 64;
		float right = this.getX() + this.getWidth() - 32;

		nvg.drawText(TranslateText.CLOUD_FAVORITES.getText(), this.getX() + 26, this.getY() + 24, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);

		if(favorites.getUsed() >= 0) {
			String quota = String.format(TranslateText.CLOUD_FAVORITES_QUOTA.getText(), favorites.getUsed(), favorites.getLimit());
			nvg.drawText(quota, this.getX() + this.getWidth() - 26 - nvg.getTextWidth(quota, 9, Fonts.REGULAR), this.getY() + 27, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
		}

		nvg.drawRect(this.getX() + 15, this.getY() + 42, this.getWidth() - 30, 1, palette.getBackgroundColor(ColorType.NORMAL));

		favoritesBox.setPosition(this.getX() + 32, this.getY() + 50, fieldWidth - 24, 18);
		favoritesBox.setDefaultText(TranslateText.CLOUD_FAVORITES_SEARCH.getText());
		favoritesBox.draw(mouseX, mouseY, partialTicks);
		MouseCursor.pointer(mouseX, mouseY, right - 18, this.getY() + 50, 18, 18);
		nvg.drawText(Icon.SEARCH, right - 14, this.getY() + 53.5F, palette.getFontColor(ColorType.DARK), 11, Fonts.ICON);

		for(int i = 0; i < CloudFavorites.VIEW_SIZE; i++) {

			int index = favorites.getView() * CloudFavorites.VIEW_SIZE + i;

			if(index >= entries.size()) {
				break;
			}

			CloudFavorites.Entry entry = entries.get(index);
			float rowY = this.getY() + 76 + (i * 25);
			Color fontColor = entry.available ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL);

			nvg.drawRoundedRect(this.getX() + 32, rowY, fieldWidth, 22, 5, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawText(nvg.getLimitText(entry.available ? entry.title : TranslateText.CLOUD_FAVORITES_UNAVAILABLE.getText(), 10, Fonts.MEDIUM, 180), this.getX() + 40, rowY + 6.5F, fontColor, 10, Fonts.MEDIUM);

			if(entry.available) {
				String detail = String.format(TranslateText.CLOUD_FAVORITES_BY.getText(), entry.author) + (entry.version.isEmpty() ? "" : " | " + entry.version);
				nvg.drawText(nvg.getLimitText(detail, 9, Fonts.REGULAR, 115), this.getX() + 230, rowY + 7.5F, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
				MouseCursor.pointer(mouseX, mouseY, right - 40, rowY + 4, 15, 14);
				nvg.drawText(Icon.DOWNLOAD, right - 38, rowY + 5.5F, favorites.isWorking(entry) ? palette.getFontColor(ColorType.NORMAL) : palette.getFontColor(ColorType.DARK), 11, Fonts.ICON);
			}

			if(entry.id.equals(removing)) {
				nvg.drawRoundedRect(right - 23, rowY + 3, 17, 16, 4, palette.getMaterialRed(60));
			}

			MouseCursor.pointer(mouseX, mouseY, right - 22, rowY + 4, 15, 14);
			nvg.drawText(Icon.TRASH, right - 20, rowY + 5.5F, palette.getMaterialRed(), 11, Fonts.ICON);
		}

		if(favorites.isLoaded() && !favorites.isBusy() && entries.isEmpty()) {
			nvg.drawText(nvg.getLimitText(TranslateText.CLOUD_FAVORITES_EMPTY.getText(), 10, Fonts.REGULAR, fieldWidth), this.getX() + 32, this.getY() + 82, palette.getFontColor(ColorType.NORMAL), 10, Fonts.REGULAR);
		}

		String status = removing != null ? TranslateText.CLOUD_FAVORITES_CONFIRM.getText() : favorites.getStatus().isEmpty() ? TranslateText.CLOUD_FAVORITES_HINT.getText() : favorites.getStatus();
		Color statusColor = favorites.isFailed() || removing != null ? palette.getMaterialRed() : palette.getFontColor(ColorType.NORMAL);
		float lineY = this.getY() + 178;

		for(String line : wrap(nvg, status, fieldWidth, 2)) {
			nvg.drawText(line, this.getX() + 32, lineY, statusColor, 9, Fonts.REGULAR);
			lineY+=13;
		}

		int position = favorites.getPosition();
		String pager = pager(favorites);
		float pagerWidth = nvg.getTextWidth(pager, 9, Fonts.REGULAR);
		float pagerY = this.getY() + this.getHeight() - 44;

		MouseCursor.pointer(mouseX, mouseY, this.getX() + 30, pagerY + 3, 15, 15);
		MouseCursor.pointer(mouseX, mouseY, this.getX() + 54 + pagerWidth, pagerY + 3, 15, 15);
		nvg.drawText(Icon.ARROW_LEFT, this.getX() + 32, pagerY + 5, position > 0 ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL), 11, Fonts.ICON);
		nvg.drawText(pager, this.getX() + 50, pagerY + 6.5F, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
		nvg.drawText(Icon.ARROW_RIGHT, this.getX() + 56 + pagerWidth, pagerY + 5, position + CloudFavorites.VIEW_SIZE < favorites.getTotal() ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL), 11, Fonts.ICON);

		nvg.drawRoundedRect(this.getX() + this.getWidth() - 124, pagerY, 100, 21, 6, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawCenteredText(TranslateText.CLOUD_REFRESH.getText(), this.getX() + this.getWidth() - 124 + 50, pagerY + 6.5F,
				favorites.isBusy() ? palette.getFontColor(ColorType.NORMAL) : palette.getFontColor(ColorType.DARK), 10, Fonts.REGULAR);
	}

	private void favoritesClicked(int mouseX, int mouseY, int mouseButton) {

		NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
		CloudFavorites favorites = CloudFavorites.getInstance();
		List<CloudFavorites.Entry> entries = favorites.getEntries();
		float right = this.getX() + this.getWidth() - 32;
		float pagerY = this.getY() + this.getHeight() - 44;
		String confirm = removing;

		favoritesBox.mouseClicked(mouseX, mouseY, mouseButton);
		removing = null;

		if(mouseButton != 0) {
			return;
		}

		if(MouseUtils.isInside(mouseX, mouseY, right - 18, this.getY() + 50, 18, 18)
				|| MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124, pagerY, 100, 21)) {
			favorites.refresh(favoritesBox.getText(), 0, false);
			return;
		}

		if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 30, pagerY + 3, 15, 15)) {
			favorites.previous();
			return;
		}

		if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 54 + nvg.getTextWidth(pager(favorites), 9, Fonts.REGULAR), pagerY + 3, 15, 15)) {
			favorites.next();
			return;
		}

		for(int i = 0; i < CloudFavorites.VIEW_SIZE; i++) {

			int index = favorites.getView() * CloudFavorites.VIEW_SIZE + i;

			if(index >= entries.size()) {
				break;
			}

			CloudFavorites.Entry entry = entries.get(index);
			float rowY = this.getY() + 76 + (i * 25);

			if(entry.available && MouseUtils.isInside(mouseX, mouseY, right - 40, rowY + 4, 15, 14)) {
				favorites.download(entry);
			}

			if(MouseUtils.isInside(mouseX, mouseY, right - 22, rowY + 4, 15, 14) && !favorites.isBusy()) {
				if(entry.id.equals(confirm)) {
					favorites.remove(entry);
				} else {
					removing = entry.id;
				}
			}
		}
	}

	private static String pager(CloudFavorites favorites) {
		int views = Math.max(1, (favorites.getTotal() + CloudFavorites.VIEW_SIZE - 1) / CloudFavorites.VIEW_SIZE);
		return Math.min(views, favorites.getPosition() / CloudFavorites.VIEW_SIZE + 1) + " / " + views;
	}

	private static List<String> wrap(NanoVGManager nvg, String text, float width, int maxLines) {

		List<String> lines = new ArrayList<String>();
		StringBuilder line = new StringBuilder();

		for(int offset = 0; offset < text.length() && lines.size() < maxLines;) {
			int codepoint = text.codePointAt(offset);
			String character = new String(Character.toChars(codepoint));
			offset+=Character.charCount(codepoint);

			if(line.length() > 0 && nvg.getTextWidth(line.toString() + character, 9, Fonts.REGULAR) > width) {
				lines.add(line.toString());
				line.setLength(0);
			}

			line.append(character);
		}

		if(line.length() > 0 && lines.size() < maxLines) {
			lines.add(line.toString());
		}

		return lines;
	}

	private boolean filter(Profile p) {
		
		if(currentType.equals(ProfileType.FAVORITE) && !p.getType().equals(ProfileType.FAVORITE)) {
			return true;
		}
		
		if(!this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(p.getName(), this.getSearchBox().getText())) {
			return true;
		}
		
		return false;
	}
}
