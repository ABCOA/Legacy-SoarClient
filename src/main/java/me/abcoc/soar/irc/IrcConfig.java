package me.abcoc.soar.irc;

import com.google.gson.Gson;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class IrcConfig {
    public static final String HOST = "irc.soarclient.net";
    public static final int PORT = 831;
    public boolean shareLocation = false;
    public boolean shareParty = false;
    public static File file() { return new File(Soar.getInstance().getFileManager().getSoarDir(), "IRC.json"); }
    public static IrcConfig load() {
        IrcConfig config = new IrcConfig();
        if (!file().isFile()) { config.save(); return config; }
        try (Reader reader = new InputStreamReader(new FileInputStream(file()), StandardCharsets.UTF_8)) {
            IrcConfig loaded = new Gson().fromJson(reader, IrcConfig.class);
            if (loaded == null) throw new IOException("Empty IRC configuration");
            return loaded;
        } catch (IOException | RuntimeException e) {
            SoarLogger.warn("Cannot read IRC configuration: " + e.getMessage());
            return config;
        }
    }
    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file()), StandardCharsets.UTF_8)) {
            new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
        } catch (IOException e) { throw new IllegalStateException("Cannot save IRC configuration", e); }
    }
}
