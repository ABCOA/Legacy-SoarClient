package me.eldodebug.soar.management.profile;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public final class ProfileFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private ProfileFiles() { }

    public static JsonObject read(Path path) throws IOException {
        if (Files.size(path) > MAX_BYTES) throw new IOException("Configuration too large");
        byte[] bytes;
        try (InputStream input = Files.newInputStream(path); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192]; int length;
            while ((length = input.read(chunk)) != -1) {
                if (out.size() + length > MAX_BYTES) throw new IOException("Configuration too large");
                out.write(chunk, 0, length);
            }
            bytes = out.toByteArray();
        }
        String text;
        try { text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
        catch (CharacterCodingException legacy) { text = Charset.defaultCharset().newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
        return parse(text);
    }
    public static JsonObject parse(String text) throws IOException {
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        try (JsonReader reader = new JsonReader(new StringReader(text))) {
            reader.setLenient(false); JsonElement value = value(reader, 0);
            if (reader.peek() != JsonToken.END_DOCUMENT || !value.isJsonObject()) throw new IOException("Invalid configuration object");
            return value.getAsJsonObject();
        } catch (RuntimeException invalid) { throw new IOException("Invalid configuration JSON", invalid); }
    }
    private static JsonElement value(JsonReader reader, int depth) throws IOException {
        if (depth > 64) throw new IOException("Configuration nesting limit");
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                JsonObject object = new JsonObject(); Set<String> names = new HashSet<String>(); reader.beginObject();
                while (reader.hasNext()) { String name = reader.nextName(); if (!names.add(name)) throw new IOException("Duplicate configuration key"); object.add(name, value(reader, depth + 1)); }
                reader.endObject(); return object;
            case BEGIN_ARRAY:
                JsonArray array = new JsonArray(); reader.beginArray(); while (reader.hasNext()) array.add(value(reader, depth + 1)); reader.endArray(); return array;
            case BOOLEAN: return new JsonPrimitive(reader.nextBoolean());
            case NUMBER: return new JsonPrimitive(new BigDecimal(reader.nextString()));
            case STRING: return new JsonPrimitive(reader.nextString());
            case NULL: reader.nextNull(); return JsonNull.INSTANCE;
            default: throw new IOException("Invalid JSON value");
        }
    }
    public static JsonObject copy(JsonObject object) { return new JsonParser().parse(object.toString()).getAsJsonObject(); }
    public static void write(Path path, JsonObject object) throws IOException {
        byte[] bytes = GSON.toJson(object).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IOException("Configuration too large");
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(path.toAbsolutePath().getParent(), ".profile-", ".tmp");
        try {
            try (FileChannel out = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes); while (buffer.hasRemaining()) out.write(buffer); out.force(true);
            }
            if (Files.isRegularFile(path)) {
                // Never replace the last known good backup with an already corrupt file.
                try { read(path); Files.copy(path, backup(path), StandardCopyOption.REPLACE_EXISTING); }
                catch (IOException invalid) { Files.copy(path, path.resolveSibling(path.getFileName() + ".invalid-" + UUID.randomUUID())); }
            }
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public static Path backup(Path path) { return path.resolveSibling(path.getFileName() + ".bak"); }
    public static JsonObject recovered(Path path) throws IOException {
        try { return read(path); }
        catch (IOException invalid) {
            if (!Files.isRegularFile(backup(path))) throw invalid;
            JsonObject good = read(backup(path)); write(path, good); return good;
        }
    }
    public static String hash(JsonElement object) {
        try {
            StringBuilder result = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(object.toString().getBytes(StandardCharsets.UTF_8))) result.append(String.format(Locale.ROOT, "%02x", b & 255));
            return result.toString();
        } catch (Exception impossible) { throw new IllegalStateException(impossible); }
    }
}
