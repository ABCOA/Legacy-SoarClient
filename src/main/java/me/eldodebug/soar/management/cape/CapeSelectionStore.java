package me.eldodebug.soar.management.cape;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;

public final class CapeSelectionStore {
    private final File file;
    private final Properties values = new Properties();

    public CapeSelectionStore(File file) throws IOException {
        this.file = file;
        if (file.isFile()) try (InputStream in = Files.newInputStream(file.toPath())) { values.load(in); }
    }

    public synchronized String getSelection(UUID account) {
        return values.getProperty(account + ".selected", "official");
    }
    public synchronized void setSelection(UUID account, String selection) {
        values.setProperty(account + ".selected", selection);
    }
    public synchronized String getPublished(UUID account) {
        return values.getProperty(account + ".published", "official");
    }
    public synchronized void setPublished(UUID account, String descriptor) {
        values.setProperty(account + ".published", descriptor);
    }
    public synchronized String getCanonicalHash(UUID account, String rawHash) {
        return values.getProperty(account + ".hash." + rawHash);
    }
    public synchronized void setCanonicalHash(UUID account, String rawHash, String canonicalHash) {
        values.setProperty(account + ".hash." + rawHash, canonicalHash);
    }
    public synchronized void removeCanonicalHash(UUID account, String rawHash) {
        values.remove(account + ".hash." + rawHash);
    }
    public synchronized void save() throws IOException {
        Files.createDirectories(file.toPath().toAbsolutePath().getParent());
        File temporary = new File(file.getPath() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temporary.toPath())) {
            values.store(out, "Soar cape selections by Minecraft UUID");
        }
        try { Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
