package me.eldodebug.soar.management.mods.impl.rearview;

import java.nio.IntBuffer;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public class RearviewCamera {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private int mirrorFBO;
	private int mirrorTex;
	private int mirrorDepth;
	private long renderEndNanoTime;
	private RenderGlobalHelper mirrorRenderGlobal;
	private float fov;
	private boolean firstUpdate, recording, lockCamera;
	
	public RearviewCamera() {
        mirrorFBO = ARBFramebufferObject.glGenFramebuffers();
        mirrorTex = GL11.glGenTextures();
        mirrorDepth = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, 800, 600, 0, GL11.GL_RGBA, GL11.GL_INT,
                (IntBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 800, 600, 0, GL11.GL_DEPTH_COMPONENT,
                GL11.GL_INT, (IntBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        
        mirrorRenderGlobal = new RenderGlobalHelper();
        fov = 70;
        lockCamera = true;
	}
	
    public void updateMirror() {
    	
        int w, h;
        float y, py, p, pp;
        boolean hide;
        int view, limit;
        long endTime = 0;
        
        GuiScreen currentScreen;
        
        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers();
            this.firstUpdate = true;
        }
        
        w = mc.displayWidth;
        h = mc.displayHeight;
        y = ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw;
        py = ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw;
        p = ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch;
        pp = ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch;
        hide = mc.gameSettings.hideGUI;
        view = mc.gameSettings.thirdPersonView;
        limit = mc.gameSettings.limitFramerate;
        fov = mc.gameSettings.fovSetting;
        currentScreen = mc.currentScreen;
    	
        switchToFB();

        if (limit != 0) {
        	endTime = renderEndNanoTime;
        }

        mc.currentScreen = null;
        mc.displayHeight = 600;
        mc.displayWidth = 800;
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.thirdPersonView = 0;
        mc.gameSettings.limitFramerate = 0;
        mc.gameSettings.fovSetting = fov;

        ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw += 180;
        ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw += 180;
        
        if(lockCamera) {
            ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = 0;
            ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = 0;
        }else {
            ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = -p + 18;
            ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = -pp + 18;
        }

        recording = true;
        mirrorRenderGlobal.switchTo();

        GL11.glPushAttrib(272393);
        
        mc.entityRenderer.renderWorld(((IMixinMinecraft)mc).getTimer().renderPartialTicks, System.nanoTime());
        mc.entityRenderer.setupOverlayRendering();
        
        if (limit != 0) {
        	renderEndNanoTime = endTime;
        }
        
        GL11.glPopAttrib();
        
        mirrorRenderGlobal.switchFrom();
        recording = false;
        
        mc.currentScreen = currentScreen;
        ((IMixinMinecraft)mc).getRenderViewEntity().rotationYaw = y;
        ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationYaw = py;
        ((IMixinMinecraft)mc).getRenderViewEntity().rotationPitch = p;
        ((IMixinMinecraft)mc).getRenderViewEntity().prevRotationPitch = pp;
        mc.gameSettings.limitFramerate = limit;
        mc.gameSettings.thirdPersonView = view;
        mc.gameSettings.hideGUI = hide;
        mc.displayWidth = w;
        mc.displayHeight = h;
        mc.gameSettings.fovSetting = fov;

        switchFromFB();
    }
	
    private void switchToFB() {
    	
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		GlStateManager.disableLighting();
		
    	OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, mirrorFBO);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
        		OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                mirrorTex, 0);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
        		OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                mirrorDepth, 0);
    }

    private void switchFromFB() {
    	OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
    }
    
    public int getTexture() {
    	return mirrorTex;
    }

	public boolean isRecording() {
		return recording;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public void setLockCamera(boolean lockCamera) {
		this.lockCamera = lockCamera;
	}
}
