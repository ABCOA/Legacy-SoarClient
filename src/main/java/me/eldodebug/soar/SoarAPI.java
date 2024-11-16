package me.eldodebug.soar;

import java.io.File;

import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.management.file.FileManager;

public class SoarAPI {

	private long launchTime;
	private GuiModMenu modMenu;
	private GuiSoarMainMenu mainMenu;
	private File firstLoginFile;
	
	public SoarAPI() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		
		firstLoginFile = new File(fileManager.getCacheDir(), "first.tmp");
	}
	
	public void init() {
		launchTime = System.currentTimeMillis();
		modMenu = new GuiModMenu();
		mainMenu = new GuiSoarMainMenu();
	}
	
	public boolean isSpecialUser() {
		return true;
	}
	
	public GuiModMenu getModMenu() {
		return modMenu;
	}

	public long getLaunchTime() {
		return launchTime;
	}

	public GuiSoarMainMenu getMainMenu() {
		return mainMenu;
	}

	public void createFirstLoginFile() {
		Soar.getInstance().getFileManager().createFile(firstLoginFile);
	}
	
	public boolean isFirstLogin() {
		return !firstLoginFile.exists();
	}
}
