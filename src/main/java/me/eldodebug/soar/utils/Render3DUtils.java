package me.eldodebug.soar.utils;

import java.awt.Color;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class Render3DUtils {

	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static void drawFillBox(AxisAlignedBB box) {
		
		GlStateManager.disableCull();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
		tessellator.draw();

		worldrenderer.begin(6, DefaultVertexFormats.POSITION);
		worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
		worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
		worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
		tessellator.draw();

		GlStateManager.enableCull();
	}
	
    public static void drawBoundingBox(final AxisAlignedBB aa) {

    	GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.maxZ));
        glVertex3D(getRenderPos(aa.minX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.minX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.minZ));
        glVertex3D(getRenderPos(aa.maxX, aa.maxY, aa.maxZ));
        glVertex3D(getRenderPos(aa.maxX, aa.minY, aa.maxZ));
        GL11.glEnd();
    }
    
    private static void glVertex3D(Vec3 vector3d) {
        GL11.glVertex3d(vector3d.xCoord, vector3d.yCoord, vector3d.zCoord);
    }
    
    private static Vec3 getRenderPos(double x, double y, double z) {

        x -= ((IMixinRenderManager)mc.getRenderManager()).getRenderPosX();
        y -= ((IMixinRenderManager)mc.getRenderManager()).getRenderPosY();
        z -= ((IMixinRenderManager)mc.getRenderManager()).getRenderPosZ();

        return new Vec3(x, y, z);
    }
	
    public static void drawTargetIndicator(Entity entity, double rad, Color color) {
    	
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDepthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks - (((IMixinRenderManager)mc.getRenderManager())).getRenderPosX();
        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks - (((IMixinRenderManager)mc.getRenderManager())).getRenderPosY()) + Math.sin(System.currentTimeMillis() / 2E+2) + 1;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ((IMixinMinecraft)mc).getTimer().renderPartialTicks - (((IMixinRenderManager)mc.getRenderManager())).getRenderPosZ();

        Color c;

        for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / 64.F) {
        	
            double vecX = x + rad * Math.cos(i);
            double vecZ = z + rad * Math.sin(i);

            c = color;

            GL11.glColor4f(c.getRed() / 255.F,
                    c.getGreen() / 255.F,
                    c.getBlue() / 255.F,
                    0
            );
            GL11.glVertex3d(vecX, y - Math.cos(System.currentTimeMillis() / 2E+2) / 2.0F, vecZ);
            GL11.glColor4f(c.getRed() / 255.F,
                    c.getGreen() / 255.F,
                    c.getBlue() / 255.F,
                    0.40F
            );
            GL11.glVertex3d(vecX, y, vecZ);
        }

        GL11.glEnd();
    	GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glDisable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        GL11.glColor3f(255, 255, 255);
    }
    
    private static void drawFilledCircleNoGL(final int x, final int y, final double r, final int c, final int quality) {
    	
        final float f = ((c >> 24) & 0xff) / 255F;
        final float f1 = ((c >> 16) & 0xff) / 255F;
        final float f2 = ((c >> 8) & 0xff) / 255F;
        final float f3 = (c & 0xff) / 255F;

        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        for (int i = 0; i <= 360 / quality; i++) {
            final double x2 = Math.sin(((i * quality * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * quality * Math.PI) / 180)) * r;
            GL11.glVertex2d(x + x2, y + y2);
        }

        GL11.glEnd();
    }
    
    public static void renderBreadCrumbs(final List<Vec3> vec3s, Color color) {

        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int i = 0;
        try {
            for (final Vec3 v : vec3s) {

                i++;

                boolean draw = true;

                final double x = v.xCoord - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosX();
                final double y = v.yCoord -  ((IMixinRenderManager)mc.getRenderManager()).getRenderPosY();
                final double z = v.zCoord - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosZ();

                final double distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord);
                int quality = (int) (distanceFromPlayer * 4 + 10);

                if (quality > 350)
                    quality = 350;

                if (i % 10 != 0 && distanceFromPlayer > 25) {
                    draw = false;
                }

                if (i % 3 == 0 && distanceFromPlayer > 15) {
                    draw = false;
                }

                if (draw) {

                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, z);

                    final float scale = 0.04f;
                    GL11.glScalef(-scale, -scale, -scale);

                    GL11.glRotated(-(mc.getRenderManager()).playerViewY, 0.0D, 1.0D, 0.0D);
                    GL11.glRotated((mc.getRenderManager()).playerViewX, 1.0D, 0.0D, 0.0D);

                    final Color c = color;

                    drawFilledCircleNoGL(0, 0, 0.7, new Color(c.getRed(), c.getGreen(), c.getBlue(), 100).hashCode(), quality);

                    if (distanceFromPlayer < 4) {
                    	drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);
                    }

                    if (distanceFromPlayer < 20) {
                    	drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);
                    }

                    GL11.glScalef(0.8f, 0.8f, 0.8f);

                    GL11.glPopMatrix();

                }

            }
        } catch (final ConcurrentModificationException ignored) {
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.enableDepth();

        GL11.glColor3d(255, 255, 255);
    }
}
