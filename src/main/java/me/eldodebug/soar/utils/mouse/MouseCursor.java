package me.eldodebug.soar.utils.mouse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/** Collects clickable hit areas during a GUI frame and updates the native cursor once. */
public final class MouseCursor {

    private static Cursor pointer;
    private static Cursor previousCursor;
    private static Pointer systemPointer, previousSystemCursor;
    private static boolean active, interactive, requested, applied, unavailable;

    private MouseCursor() {}

    public static void beginFrame() {
        active = true;
        interactive = true;
        requested = false;
    }

    public static boolean isInteractive() {
        return active && interactive;
    }

    public static void setInteractive(boolean value) {
        interactive = value;
    }

    public static void pointer(int mouseX, int mouseY, double x, double y, double width, double height) {
        if (isInteractive() && MouseUtils.isInside(mouseX, mouseY, x, y, width, height)) {
            requested = true;
        }
    }

    public static void endFrame() {
        boolean usePointer = active && requested;
        active = false;
        if (!Mouse.isCreated() || Mouse.isGrabbed() || !Mouse.isInsideWindow()) {
            restore();
            return;
        }
        if (!usePointer || unavailable) {
            restore();
            return;
        }
        try {
            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
                applyWindowsPointer();
                return;
            }
            if (pointer == null) {
                pointer = createPointer();
            }
            if (!applied) {
                previousCursor = Mouse.getNativeCursor();
                Mouse.setNativeCursor(pointer);
                applied = true;
            }
        } catch (LWJGLException | RuntimeException | LinkageError e) {
            unavailable = true;
            restore();
            LWJGLUtil.log("Unable to set menu cursor: " + e);
        }
    }

    public static void reset() {
        active = false;
        requested = false;
        restore();
        // LoadCursorW returns shared system resources; they must not be destroyed.
        systemPointer = null;
        if (pointer != null && Mouse.isCreated()) {
            pointer.destroy();
            pointer = null;
        }
    }

    private static void restore() {
        if (applied && LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
            // Once the mouse leaves our window, its new window owns the cursor.
            // A grabbed mouse is already hidden by LWJGL, so leave it alone.
            if (Mouse.isCreated() && !Mouse.isGrabbed() && Mouse.isInsideWindow()) {
                WindowsUser32.INSTANCE.SetCursor(previousSystemCursor);
            }
            applied = false;
            previousSystemCursor = null;
            return;
        }
        if (applied && Mouse.isCreated()) {
            try {
                Mouse.setNativeCursor(previousCursor);
            } catch (LWJGLException e) {
                return;
            }
        }
        applied = false;
        previousCursor = null;
    }

    private static void applyWindowsPointer() {
        if (systemPointer == null) {
            // IDC_HAND uses the user's Windows cursor scheme, size and hotspot.
            systemPointer = WindowsUser32.INSTANCE.LoadCursorW(null, Pointer.createConstant(32649));
            if (systemPointer == null) {
                throw new IllegalStateException("Windows could not load IDC_HAND");
            }
        }
        Pointer previous = WindowsUser32.INSTANCE.SetCursor(systemPointer);
        if (!applied) {
            previousSystemCursor = previous;
            applied = true;
        }
    }

    /** Keeps Windows mouse messages from replacing the hand with the class cursor. */
    public static boolean applySystemPointer() {
        if (!applied || unavailable || !Mouse.isCreated() || Mouse.isGrabbed()
                || LWJGLUtil.getPlatform() != LWJGLUtil.PLATFORM_WINDOWS) {
            return false;
        }
        applyWindowsPointer();
        return true;
    }

    public interface WindowsUser32 extends StdCallLibrary {
        WindowsUser32 INSTANCE = (WindowsUser32) Native.loadLibrary("user32", WindowsUser32.class);
        Pointer LoadCursorW(Pointer instance, Pointer name);
        Pointer SetCursor(Pointer cursor);
    }

    private static Cursor createPointer() throws LWJGLException {
        int size = Math.max(Cursor.getMinCursorSize(), Math.min(32, Cursor.getMaxCursorSize()));
        BufferedImage image = createPointerImage(size);
        IntBuffer pixels = BufferUtils.createIntBuffer(size * size);
        // LWJGL expects the image and hotspot with an origin at the bottom left.
        for (int y = size - 1; y >= 0; y--) {
            for (int x = 0; x < size; x++) {
                pixels.put(image.getRGB(x, y));
            }
        }
        pixels.flip();
        float scale = size / 32F;
        return new Cursor(size, size, Math.round(11 * scale), size - 1 - Math.round(2 * scale), 1, pixels, null);
    }

    private static BufferedImage createPointerImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        float scale = size / 32F;
        graphics.scale(scale, scale);
        Path2D.Float hand = new Path2D.Float();
        hand.moveTo(9, 16);
        hand.lineTo(9, 4);
        hand.curveTo(9, 0, 13, 0, 13, 4);
        hand.lineTo(13, 12);
        hand.curveTo(13, 8, 17, 8, 17, 12);
        hand.curveTo(17, 9, 21, 9, 21, 13);
        hand.curveTo(21, 10, 25, 10, 25, 14);
        hand.lineTo(25, 21);
        hand.curveTo(25, 24, 23, 25, 23, 29);
        hand.lineTo(12, 29);
        hand.curveTo(12, 25, 8, 24, 5, 19);
        hand.curveTo(2, 14, 5, 12, 9, 16);
        hand.closePath();
        graphics.setColor(Color.WHITE);
        graphics.fill(hand);
        graphics.setColor(new Color(35, 35, 35));
        graphics.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(hand);
        graphics.drawLine(13, 13, 13, 19);
        graphics.drawLine(17, 13, 17, 19);
        graphics.drawLine(21, 14, 21, 19);
        graphics.dispose();

        return image;
    }
}
