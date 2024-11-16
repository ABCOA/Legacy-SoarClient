package me.eldodebug.soar.mcef;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.GlStateManager;

public class McefRenderer {

	private final boolean transparent;
	private final int[] textureID = new int[1];
	
	protected McefRenderer(boolean transparent) {
		this.transparent = transparent;
	}
	
	public void initialize() {
		textureID[0] = GL11.glGenTextures();
		GlStateManager.bindTexture(textureID[0]);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.bindTexture(0);
	}
	
	public int getTextureID() {
		return textureID[0];
	}
	
	public boolean isTransparent() {
		return transparent;
	}
	
	protected void cleanup() {
		if(textureID[0] != 0) {
			GL11.glDeleteTextures(textureID[0]);
			textureID[0] = 0;
		}
	}
	
	protected void onPaint(ByteBuffer buffer, int width, int height) {
		
		if(textureID[0] == 0) {
			return;
		}
		
		if(transparent) {
			GlStateManager.enableBlend();
		}
		
		GlStateManager.bindTexture(textureID[0]);
        GL11.glPixelStoref(GL11.GL_UNPACK_ROW_LENGTH, width);
        GL11.glPixelStoref(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStoref(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
        		GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
	}
	
    protected void onPaint(ByteBuffer buffer, int x, int y, int width, int height) {
    	GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height, GL12.GL_BGRA,
    			GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
    }
}
