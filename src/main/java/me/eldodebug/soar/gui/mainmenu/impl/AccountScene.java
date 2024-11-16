package me.eldodebug.soar.gui.mainmenu.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.gui.mainmenu.impl.welcome.LastMessageScene;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.management.account.Account;
import me.eldodebug.soar.management.account.AccountManager;
import me.eldodebug.soar.management.account.AccountType;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.mcef.Mcef;
import me.eldodebug.soar.ui.comp.impl.field.CompMainMenuTextBox;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

public class AccountScene extends MainMenuScene {

	public CompMainMenuTextBox textBox = new CompMainMenuTextBox();
	
	private File offlineSkin;
	
	public AccountScene(GuiSoarMainMenu parent) {
		super(parent);
	}

	@Override
	public void initScene() {
		if(!Mcef.isInitialized()) {
			Mcef.getClient();
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		
		nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg));
	}
	
	public void drawNanoVG(int mouseX, int mouseY, float partialTicks, ScaledResolution sr, Soar instance, NanoVGManager nvg) {
		
		int acWidth = 220;
		int acHeight = 138;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		
		String loginMessage = TranslateText.LOGIN_MESSAGE.getText();
		String microsoftLogin = TranslateText.MICROSOFT_LOGIN.getText();
		String offlineLogin = TranslateText.OFFLINE_LOGIN.getText();
		
		nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
		nvg.drawCenteredText(loginMessage, acX + (acWidth / 2), acY + 9, Color.WHITE, 14, Fonts.REGULAR);
		
		nvg.drawRoundedImage(new ResourceLocation("soar/mainmenu/microsoft-background.png"), acX + 10, acY + 29, 200, 30, 5);
		
		nvg.drawText(microsoftLogin, acX + 45, acY + 40, Color.WHITE, 10, Fonts.REGULAR);
		
		nvg.drawRoundedRect(acX + 18, acY + 34, 9, 9, 1, new Color(247, 78, 30));
		nvg.drawRoundedRect(acX + 18 + 11, acY + 34, 9, 9, 1, new Color(127, 186, 0));
		nvg.drawRoundedRect(acX + 18, acY + 34 + 11, 9, 9, 1, new Color(0, 164, 239));
		nvg.drawRoundedRect(acX + 18 + 11, acY + 34 + 11, 9, 9, 1, new Color(255, 185, 0));
		
		nvg.drawCenteredText(offlineLogin, acX + (acWidth / 2), acY + 67, Color.WHITE, 14, Fonts.REGULAR);
		
		nvg.drawRoundedRect(acX + acWidth - 30, acY + 86, 20, 20, 4, this.getBackgroundColor());
		nvg.drawText(Icon.USER, acX + acWidth - 25, acY + 91, Color.WHITE, 10, Fonts.ICON);
		
		textBox.setBackgroundColor(this.getBackgroundColor());
		textBox.setFontColor(Color.WHITE);
		textBox.setPosition(acX + 10, acY + 86, 175, 20);
		textBox.setEmptyText(Icon.PENCIL, TranslateText.NAME.getText());
		textBox.draw(mouseX, mouseY, partialTicks);
		
		nvg.drawRoundedRect(acX + acWidth - 96 - 10, acY + 86 + 25, 96, 20, 4, this.getBackgroundColor());
		nvg.drawCenteredText(TranslateText.LOGIN.getText(), acX + acWidth - (96 / 2) - 10, acY + 86 + 31, Color.WHITE, 10F, Fonts.REGULAR);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		
		Soar instance = Soar.getInstance();
		AccountManager accountManager = instance.getAccountManager();
		FileManager fileManager = instance.getFileManager();
		
		int acWidth = 220;
		int acHeight = 140;
		int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
		int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
		
		if(mouseButton == 0) {
			
			if(MouseUtils.isInside(mouseX, mouseY, acX + acWidth - 30, acY + 86, 20, 20)) {
				Multithreading.runAsync(() -> {
					
					File selectSkin = FileUtils.selectImageFile();
					
					if(selectSkin != null) {
						
						try {
							
							File copyFile = new File(fileManager.getCacheDir(), "skin/" + selectSkin.getName());
							BufferedImage image = ImageIO.read(selectSkin);
							
							if(image.getWidth() == 64 && image.getHeight() == 64) {
								FileUtils.copyFile(selectSkin, copyFile);
								offlineSkin = copyFile;
							}
							
						} catch(Exception e) {}
					}
				});
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, acX + acWidth - 96 - 10, acY + 86 + 25, 96, 20)) {
				
				File renameFile = new File(fileManager.getCacheDir(), "skin/" + textBox.getText() + ".png");
				File headDir = new File(fileManager.getCacheDir(), "head");
				
				Account acc = new Account(textBox.getText(), "0", "0", AccountType.OFFLINE);
				
				if(offlineSkin != null) {
					offlineSkin.renameTo(renameFile);
					offlineSkin = renameFile;
				}
				
				if(offlineSkin != null && offlineSkin.exists()) {
					
					acc.setSkinFile(offlineSkin);
					
					try {
						
						BufferedImage rawImage = ImageIO.read(offlineSkin);
						BufferedImage headImage = ImageUtils.scissor(rawImage, 8, 8, 8, 8);
						BufferedImage layerImage = ImageUtils.scissor(rawImage, 40, 8, 8, 8);
						BufferedImage conbineImage = ImageUtils.combine(headImage, layerImage);
						
						ImageIO.write(ImageUtils.resize(conbineImage, 128, 128), "png", new File(headDir, acc.getName() + ".png"));
					} catch(Exception e) {}
				}
				
		        ((IMixinMinecraft) mc).setSession(new Session(acc.getName(), "0", "0", "mojang"));
		        
				if(accountManager.getAccountByName(acc.getName()) == null) {
					accountManager.getAccounts().add(acc);
				}
				
		        accountManager.setCurrentAccount(acc);
		        accountManager.save();
		        
		        offlineSkin = null;
		        
		        getAfterLoginRunnable().run();
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, acX + 10, acY + 29, 200, 30)) {
				if(Mcef.isInitialized()) {
					this.setCurrentScene(this.getSceneByClass(MicrosoftLoginScene.class));
				} else {
					accountManager.getAuthenticator().loginWithPopUpWindow(getAfterLoginRunnable());
				}
			}
		}
		
		textBox.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		textBox.keyTyped(typedChar, keyCode);
		
		if(keyCode == Keyboard.KEY_ESCAPE) {
			this.setCurrentScene(this.getSceneByClass(MainScene.class));
		}
	}
	
	private Runnable getAfterLoginRunnable() {
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
		        if(Soar.getInstance().getApi().isFirstLogin()) {
					setCurrentScene(getSceneByClass(LastMessageScene.class));
		        } else {
					setCurrentScene(getSceneByClass(MainScene.class));
		        }
			}
		};
		
		return runnable;
	}
}
