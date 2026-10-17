package me.eldodebug.soar.management.profile;

import java.io.File;

import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

public class Profile {

	private SimpleAnimation starAnimation = new SimpleAnimation();
	
	private File jsonFile;
	private int id;
	private String name, serverIp;
	private ProfileType type;
	private ProfileIcon icon;
	private String stableId, sourceId = "", sourceHash = "", importedStateHash = "";
	
	public Profile(int id, String serverIp, File jsonFile, ProfileIcon icon) {
		this.id = id;
		this.jsonFile = jsonFile;
		this.name = jsonFile != null ? jsonFile.getName().replace(".json", "") : "";
		this.serverIp = serverIp;
		this.icon = icon;
		this.type = ProfileType.ALL;
	}
	
	public int getId() {
		return id;
	}

	public File getJsonFile() {
		return jsonFile;
	}

	public ProfileIcon getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public ProfileType getType() {
		return type;
	}

	public void setType(ProfileType type) {
		this.type = type;
	}

	public SimpleAnimation getStarAnimation() {
		return starAnimation;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getStableId() { return stableId; }
	public void setStableId(String value) { stableId = value; }
	public void setName(String value) { name = value; }
	public void setIcon(ProfileIcon value) { icon = value; }
	public boolean isDefault() { return jsonFile != null && jsonFile.getName().equalsIgnoreCase("Default.json"); }
	public String getSourceId() { return sourceId; }
	public String getSourceHash() { return sourceHash; }
	public String getImportedStateHash() { return importedStateHash; }
	public void setSource(String id, String hash, String stateHash) { sourceId = id; sourceHash = hash; importedStateHash = stateHash; }
}
