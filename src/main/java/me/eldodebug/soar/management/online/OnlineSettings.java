package me.eldodebug.soar.management.online;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

final class OnlineSettings {
    boolean hidden, noticeSeen;
    private final File file;
    OnlineSettings(File directory) {
        file = new File(directory, "online.json");
        if (!file.isFile()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            hidden = json.has("hiddenUsername") && json.get("hiddenUsername").getAsBoolean();
            noticeSeen = json.has("noticeSeen") && json.get("noticeSeen").getAsBoolean();
        } catch (Exception ignored) { }
    }
    void save() {
        JsonObject json = new JsonObject();
        json.addProperty("hiddenUsername", hidden); json.addProperty("noticeSeen", noticeSeen);
        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
                new Gson().toJson(json, writer);
            }
            try { Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) { }
    }
}
