package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;

public class PlayerDisplayMod extends HUDMod {

	private NumberSetting yawOffsetSetting = new NumberSetting(TranslateText.YAW_OFFSET, this, 0, -90, 120, true);
	
	public PlayerDisplayMod() {
		super(TranslateText.PLAYER_DISPLAY, TranslateText.PLAYER_DISPLAY_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getX() + (15 * this.getScale()), this.getY() + (58 * this.getScale()), -500.0F);
        GlStateManager.scale(-this.getScale() * 30, this.getScale() * 30, this.getScale() * 30);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(mc.thePlayer.rotationYaw + yawOffsetSetting.getValueFloat(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();

        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(mc.thePlayer, 0.0D, 0.0D, 0.0D, 0.0F, event.getPartialTicks(), true);
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        
        this.setWidth(30);
        this.setHeight(60);
	}
}
