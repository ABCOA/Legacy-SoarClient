package me.eldodebug.soar.utils.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import me.eldodebug.soar.logger.SoarLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class IconFontRenderer {

	private static final int TEXTURE_SIZE = 64;
	private static final ResourceLocation FONT_LOCATION = new ResourceLocation("soar/fonts/Icon.ttf");
	private static final Map<String, ResourceLocation> ICONS = new HashMap<String, ResourceLocation>();

	private static Font iconFont;

	public static void drawIcon(String icon, float x, float y, float size, int color) {
		ResourceLocation texture = getIconTexture(icon);

		if(texture == null) {
			return;
		}

		float alpha = (float)(color >> 24 & 255) / 255.0F;
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;

		GlStateManager.color(red, green, blue, alpha);
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		RenderUtils.drawQuads(x, y, size, size);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static int getIconWidth(float size) {
		return Math.round(size);
	}

	private static ResourceLocation getIconTexture(String icon) {
		if(ICONS.containsKey(icon)) {
			return ICONS.get(icon);
		}

		Font font = getIconFont();

		if(font == null) {
			return null;
		}

		BufferedImage image = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setComposite(AlphaComposite.Clear);
		graphics.fillRect(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
		graphics.setComposite(AlphaComposite.SrcOver);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(font.deriveFont(Font.PLAIN, 52.0F));
		graphics.setColor(Color.WHITE);

		FontMetrics metrics = graphics.getFontMetrics();
		int x = (TEXTURE_SIZE - metrics.stringWidth(icon)) / 2;
		int y = ((TEXTURE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
		graphics.drawString(icon, x, y);
		graphics.dispose();

		ResourceLocation texture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("soar_badge_" + icon.hashCode(), new DynamicTexture(image));
		ICONS.put(icon, texture);
		return texture;
	}

	private static Font getIconFont() {
		if(iconFont != null) {
			return iconFont;
		}

		try (InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(FONT_LOCATION).getInputStream()) {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
		} catch(Exception e) {
			SoarLogger.error("Failed to load icon font", e);
		}

		return iconFont;
	}
}
