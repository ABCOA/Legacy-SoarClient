package me.eldodebug.soar.management.badge;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

public class BadgeManager {

	private final Map<String, Badge> badges = new HashMap<String, Badge>();
	private final Map<UUID, Badge> playerBadges = new ConcurrentHashMap<UUID, Badge>();
	private final Map<String, Badge> playerNameBadges = new ConcurrentHashMap<String, Badge>();
	private String remoteUrl = "";
	private int refreshIntervalSeconds = 300;

	public BadgeManager() {
		addBadge("soar", Icon.SOAR, 0xFFFFD24A);
		load();
		scheduleRemoteRefresh();
	}

	private void addBadge(String id, String icon, int color) {
		badges.put(id.toLowerCase(Locale.ENGLISH), new Badge(id, icon, color));
	}

	private void load() {
		FileManager fileManager = Soar.getInstance().getFileManager();
		File badgeFile = new File(fileManager.getSoarDir(), "Badges.json");

		try {
			if(!badgeFile.exists() || badgeFile.length() == 0L) {
				writeDefaultFile(badgeFile);
			}

			try (FileReader reader = new FileReader(badgeFile)) {
				JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);

				if(jsonObject == null || !jsonObject.isJsonObject()) {
					return;
				}

				remoteUrl = JsonUtils.getStringProperty(jsonObject, "url", "");
				refreshIntervalSeconds = Math.max(60, JsonUtils.getIntProperty(jsonObject, "refreshIntervalSeconds", refreshIntervalSeconds));
				loadPlayers(jsonObject, false);
			}
		} catch(Exception e) {
			SoarLogger.error("Failed to load player badges", e);
		}
	}

	private void writeDefaultFile(File badgeFile) {
		try (FileWriter writer = new FileWriter(badgeFile)) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("url", "");
			jsonObject.addProperty("refreshIntervalSeconds", refreshIntervalSeconds);
			jsonObject.add("players", new JsonArray());
			new Gson().toJson(jsonObject, writer);
		} catch(Exception e) {
			SoarLogger.error("Failed to create badge file", e);
		}
	}

	private void scheduleRemoteRefresh() {
		if(remoteUrl == null || remoteUrl.trim().isEmpty()) {
			return;
		}

		Multithreading.runAsync(this::refreshRemoteBadges);
		Multithreading.schedule(this::refreshRemoteBadges, refreshIntervalSeconds, refreshIntervalSeconds, TimeUnit.SECONDS);
	}

	private void refreshRemoteBadges() {
		try {
			JsonObject jsonObject = HttpUtils.readJson(remoteUrl, null);

			if(jsonObject == null || !jsonObject.isJsonObject()) {
				return;
			}

			loadPlayers(jsonObject, true);
			SoarLogger.info("Loaded remote player badges");
		} catch(Exception e) {
			SoarLogger.error("Failed to refresh remote player badges", e);
		}
	}

	private synchronized void loadPlayers(JsonObject jsonObject, boolean replace) {
		if(replace) {
			playerBadges.clear();
			playerNameBadges.clear();
		}

		JsonArray players = JsonUtils.getArrayProperty(jsonObject, "players");

		for(JsonElement element : players) {
			if(element == null || !element.isJsonObject()) {
				continue;
			}

			loadPlayer(element.getAsJsonObject());
		}
	}

	private void loadPlayer(JsonObject jsonObject) {
		Badge badge = getBadge(JsonUtils.getStringProperty(jsonObject, "badge", ""));

		if(badge == null) {
			return;
		}

		String uuidText = JsonUtils.getStringProperty(jsonObject, "uuid", "");
		String name = JsonUtils.getStringProperty(jsonObject, "name", "");

		if(!uuidText.isEmpty()) {
			UUID uuid = parseUuid(uuidText);

			if(uuid == null) {
				SoarLogger.warn("Invalid badge uuid: " + uuidText);
			} else {
				playerBadges.put(uuid, badge);
			}
		}

		if(!name.isEmpty()) {
			playerNameBadges.put(name.toLowerCase(Locale.ENGLISH), badge);
		}
	}

	private Badge getBadge(String id) {
		return badges.get(id.toLowerCase(Locale.ENGLISH));
	}

	private UUID parseUuid(String uuidText) {
		if(uuidText == null) {
			return null;
		}

		String normalizedUuid = uuidText.trim().replace("-", "");

		if(normalizedUuid.length() != 32) {
			return null;
		}

		try {
			return UUID.fromString(normalizedUuid.replaceFirst(
					"([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
					"$1-$2-$3-$4-$5"));
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	public Badge getBadge(GameProfile profile) {
		if(profile == null) {
			return null;
		}

		if(profile.getId() != null) {
			Badge badge = playerBadges.get(profile.getId());

			if(badge != null) {
				return badge;
			}
		}

		if(profile.getName() == null) {
			return null;
		}

		return playerNameBadges.get(profile.getName().toLowerCase(Locale.ENGLISH));
	}
}
