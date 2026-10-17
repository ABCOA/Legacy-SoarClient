package me.eldodebug.soar.ui;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Hover feedback shared by the Soar screens, using the same values as the profile page:
 * cards and secondary buttons get an accent tint, list rows a lighter tint, accent-filled
 * controls a white sheen, and icon-only controls turn to the accent color.
 */
public final class HoverStyle {

	public static final int BUTTON = 36, ROW = 20, SHEEN = 40;

	private static final Map<Object, SimpleAnimation> ANIMATIONS = new HashMap<Object, SimpleAnimation>();

	private HoverStyle() {}

	/** Hit test that follows the menu's pointer rules (no hover while a region is not interactive). */
	public static boolean isHovered(int mouseX, int mouseY, double x, double y, double width, double height) {
		return MouseCursor.isInteractive() && MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
	}

	/** Animation stored per long-lived key (an enum, a mod, a fixed string). */
	public static SimpleAnimation animation(Object key) {
		return ANIMATIONS.computeIfAbsent(key, k -> new SimpleAnimation());
	}

	public static float update(SimpleAnimation animation, boolean hovered) {
		animation.setAnimation(hovered ? 1.0F : 0.0F, 16);
		return animation.getValue();
	}

	public static float update(Object key, boolean hovered) {
		return update(animation(key), hovered);
	}

	/** Accent tint drawn over a card, chip or secondary button. */
	public static void tint(NanoVGManager nvg, float value, int strength, float x, float y, float width, float height, float radius) {
		if(value > 0.01F) {
			nvg.drawRoundedRect(x, y, width, height, radius, ColorUtils.applyAlpha(accent(), (int) (value * strength)));
		}
	}

	public static void tint(NanoVGManager nvg, float value, float x, float y, float width, float height, float radius) {
		tint(nvg, value, BUTTON, x, y, width, height, radius);
	}

	/** White sheen drawn over an accent-filled control. */
	public static void sheen(NanoVGManager nvg, float value, float x, float y, float width, float height, float radius) {
		if(value > 0.01F) {
			nvg.drawRoundedRect(x, y, width, height, radius, new Color(255, 255, 255, (int) (value * SHEEN)));
		}
	}

	/** Accent outline for controls whose fill must stay unchanged (color swatches, images). */
	public static void outline(NanoVGManager nvg, float value, float x, float y, float width, float height, float radius) {
		if(value > 0.01F) {
			nvg.drawOutlineRoundedRect(x, y, width, height, radius, 1, ColorUtils.applyAlpha(accent(), (int) (value * 255)));
		}
	}

	/** Moves an icon or text color toward the accent color. */
	public static Color blend(Color base, float value) {
		if(value <= 0.01F) {
			return base;
		}

		Color accent = accent();
		float amount = Math.min(1.0F, value), keep = 1.0F - amount;

		return new Color(
				(int) (base.getRed() * keep + accent.getRed() * amount),
				(int) (base.getGreen() * keep + accent.getGreen() * amount),
				(int) (base.getBlue() * keep + accent.getBlue() * amount),
				base.getAlpha());
	}

	private static Color accent() {
		return Soar.getInstance().getColorManager().getCurrentColor().getColor1();
	}
}
