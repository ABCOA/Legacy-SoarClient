package me.eldodebug.soar.management.file;

import java.io.File;
import java.io.IOException;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import net.minecraft.client.Minecraft;

public class FileManager {

	private File soarDir, profileDir, cacheDir, musicDir, externalDir,
					screenshotDir;
	
	public FileManager() {
		
		soarDir = new File(Minecraft.getMinecraft().mcDataDir, "soar");
		profileDir = new File(soarDir, "profile");
		cacheDir = new File(soarDir, "cache");
		musicDir = new File(soarDir, "music");
		externalDir = new File(soarDir, "external");
		screenshotDir = new File(soarDir, "screenshots");
		
		createDir(soarDir);
		createDir(profileDir);
		createDir(cacheDir);
		createDir(musicDir);
		createDir(externalDir);
		createDir(screenshotDir);
		
		createVersionFile();
	}
	
	private void createVersionFile() {
		
		File versionDir = new File(cacheDir, "version");
		
		createDir(versionDir);
		createFile(new File(versionDir, Soar.getInstance().getVersion() + ".tmp"));
	}
	
	public void createDir(File file) {
		file.mkdir();
	}
	
	public void createFile(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			SoarLogger.error("Failed to create file " + file.getName(), e);
		}
	}

	public File getScreenshotDir() {
		return screenshotDir;
	}

	public File getSoarDir() {
		return soarDir;
	}

	public File getProfileDir() {
		return profileDir;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public File getMusicDir() {
		return musicDir;
	}

	public File getExternalDir() {
		return externalDir;
	}
}
