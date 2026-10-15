package me.eldodebug.soar.management.cape;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public final class CapeImageCodec {
    public static final int MAX_PNG_BYTES = 512 * 1024;
    public static final int MAX_SOURCE_BYTES = 2 * 1024 * 1024;
    private CapeImageCodec() { }

    public static BufferedImage readSource(File file) throws IOException {
        if (!file.isFile() || file.length() > MAX_SOURCE_BYTES) throw new IOException("Source image exceeds 2 MiB");
        byte[] bytes;
        try (InputStream input = Files.newInputStream(file.toPath()); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) {
                if (output.size() + length > MAX_SOURCE_BYTES) throw new IOException("Source image exceeds 2 MiB");
                output.write(buffer, 0, length);
            }
            bytes = output.toByteArray();
        }
        return decode(bytes, false);
    }
    public static BufferedImage decodePng(byte[] bytes) throws IOException {
        if (bytes.length > MAX_PNG_BYTES) throw new IOException("Cape PNG exceeds 512 KiB");
        return decode(bytes, true);
    }
    private static BufferedImage decode(byte[] bytes, boolean requirePng) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Unsupported image");
            ImageReader reader = readers.next();
            try {
                if (requirePng && !"png".equalsIgnoreCase(reader.getFormatName())) throw new IOException("PNG required");
                reader.setInput(input, true, true);
                validateDimensions(reader.getWidth(0), reader.getHeight(0));
                BufferedImage image = reader.read(0);
                if (image == null) throw new IOException("Invalid cape image");
                return image;
            } finally { reader.dispose(); }
        }
    }
    public static void validateDimensions(int width, int height) throws IOException {
        if (width < 64 || height < 32 || width > 1024 || height > 512 || width != height * 2)
            throw new IOException("Cape dimensions must be 2:1, from 64x32 to 1024x512");
    }
    public static byte[] normalize(BufferedImage source) throws IOException {
        validateDimensions(source.getWidth(), source.getHeight());
        BufferedImage plain = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = plain.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(plain, "png", out)) throw new IOException("PNG encoder unavailable");
        byte[] bytes = out.toByteArray();
        if (bytes.length > MAX_PNG_BYTES) throw new IOException("Normalized cape PNG exceeds 512 KiB");
        return bytes;
    }
    public static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder(64);
            for (byte value : digest) result.append(String.format("%02x", value & 255));
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    public static boolean isHash(String hash) { return hash != null && hash.matches("[a-f0-9]{64}"); }
}
