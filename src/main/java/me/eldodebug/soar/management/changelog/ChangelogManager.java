package me.eldodebug.soar.management.changelog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangelogManager {

	private CopyOnWriteArrayList<Changelog> changelogs = new CopyOnWriteArrayList<Changelog>();
	
	public ChangelogManager() {
		Multithreading.runAsync(() -> loadChangelog());
	}
	
	private void loadChangelog() {
		
		JsonObject jsonObject = HttpUtils.readJson("https://file.abcoc.uk/changelog.json", null);
		
		if(jsonObject != null) {
			
			JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "changelogs");
			
			if(jsonArray != null) {
				
				Iterator<JsonElement> iterator = jsonArray.iterator();
				
				while(iterator.hasNext()) {
					
					JsonElement jsonElement = (JsonElement) iterator.next();
					Gson gson = new Gson();
					JsonObject changelogJsonObject = gson.fromJson(jsonElement, JsonObject.class);
					
					changelogs.add(new Changelog(JsonUtils.getStringProperty(changelogJsonObject, "text", "null"), 
							ChangelogType.getTypeById(JsonUtils.getIntProperty(changelogJsonObject, "type", 999))));
				}
			}
		}
	}

	public CopyOnWriteArrayList<Changelog> getChangelogs() {
		return changelogs;
	}
}
