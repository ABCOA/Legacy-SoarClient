package me.eldodebug.soar.management.screenshot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.file.FileUtils;

public class ScreenshotManager {

	private CopyOnWriteArrayList<Screenshot> screenshots = new CopyOnWriteArrayList<Screenshot>();
	private CopyOnWriteArrayList<File> removeScreenshots = new CopyOnWriteArrayList<File>();
	private int prevSize;
	
	public ScreenshotManager() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File screenshotCacheDir = new File(fileManager.getCacheDir(), "screenshot");
		File dataJson = new File(screenshotCacheDir, "Data.json");
		ArrayList<String> removeScreenshots = loadData();
		
		if(!screenshotCacheDir.exists()) {
			fileManager.createDir(screenshotCacheDir);
		}
		
		if(!dataJson.exists()) {
			fileManager.createFile(dataJson);
		}
		
		for(File f : fileManager.getScreenshotDir().listFiles()) {
			if(!removeScreenshots.isEmpty() && removeScreenshots.contains(f.getName())) {
				f.delete();
			}
		}
		
		loadScreenshots();
	}
	
	public ArrayList<String> loadData() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File screenshotCacheDir = new File(fileManager.getCacheDir(), "screenshot");
		File dataJson = new File(screenshotCacheDir, "Data.json");
		
		ArrayList<String> output = new ArrayList<String>();
		
		try (FileReader reader = new FileReader(dataJson)) {
			
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
			
			if(jsonObject != null) {
				
				JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Remove Screenshots");
				
				if(jsonArray != null) {
					
					Iterator<JsonElement> iterator = jsonArray.iterator();
					
					while(iterator.hasNext()) {
						
						JsonElement jsonElement = (JsonElement) iterator.next();
						JsonObject rJsonObject = gson.fromJson(jsonElement, JsonObject.class);
						
						output.add(JsonUtils.getStringProperty(rJsonObject, "Screenshot", "null"));
					}
				}
			}
		} catch (Exception e) {}
		
		return output;
	}
	
	public void saveData() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File screenshotCacheDir = new File(fileManager.getCacheDir(), "screenshot");
		File dataJson = new File(screenshotCacheDir, "Data.json");
		
		try(FileWriter writer = new FileWriter(dataJson)) {
			
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();
			Gson gson = new Gson();
			
			for(File f : removeScreenshots) {
				
				JsonObject innerJsonObject = new JsonObject();
				
				innerJsonObject.addProperty("Screenshot", f.getName());
				
				jsonArray.add(innerJsonObject);
			}
			
			jsonObject.add("Remove Screenshots", jsonArray);
			
			gson.toJson(jsonObject, writer);
		} catch(Exception e) {}
	}
	
	public void loadScreenshots() {
		
		File screenshotDir = Soar.getInstance().getFileManager().getScreenshotDir();
		
		if(prevSize != screenshotDir.listFiles().length) {
			
			prevSize = screenshotDir.listFiles().length;
			
			for(File f : screenshotDir.listFiles()) {
				
				if(FileUtils.getExtension(f).equals("png")) {
					if(!removeScreenshots.contains(f) && getScreenshotByFile(f) == null) {
						screenshots.add(new Screenshot(f));
						Soar.getInstance().getNanoVGManager().loadImage(f);
					}
				}
			}
		}
	}
	
	public Screenshot getNextScreenshot(Screenshot currentScreenshot) {
		
		int max = screenshots.size();
		int index = screenshots.indexOf(currentScreenshot);
		
		if(index < max - 1) {
			index++;
		}else {
			index = 0;
		}
		
		return screenshots.get(index);
	}
	
	public Screenshot getBackScreenshot(Screenshot currentScreenshot) {
		
		int max = screenshots.size();
		int index = screenshots.indexOf(currentScreenshot);
		
		if(index > 0) {
			index--;
		}else {
			index = max - 1;
		}
		
		return screenshots.get(index);
	}
	
	public Screenshot getScreenshotByFile(File file) {
		
		for(Screenshot sc : screenshots) {
			if(sc.getImage().equals(file)) {
				return sc;
			}
		}
		
		return null;
	}
	
	public void delete(Screenshot screenshot) {
		removeScreenshots.add(screenshot.getImage());
		screenshots.remove(screenshot);
		saveData();
		loadScreenshots();
	}

	public CopyOnWriteArrayList<Screenshot> getScreenshots() {
		return screenshots;
	}
}
