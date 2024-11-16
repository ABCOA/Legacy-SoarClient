package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.GuiWaypoint;
import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.waypoint.Waypoint;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class WaypointMod extends Mod {

	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_B);
	
	public WaypointMod() {
		super(TranslateText.WAYPOINT, TranslateText.WAYPOINT_DESCRIPTION, ModCategory.WORLD);
	}

	@EventTarget
	public void onRender3D(EventRender3D event) {
		
		for(Waypoint wy : Soar.getInstance().getWaypointManager().getWaypoints()) {
			if(Soar.getInstance().getWaypointManager().getWorld().equals(wy.getWorld())) {
				
				double distance = this.getDistance(wy, mc.getRenderViewEntity());
				double renderDistance = (mc.gameSettings.renderDistanceChunks * 16) * 0.75;
				
				String tagName = wy.getName() + " [" + (int) distance + "m]";
				
				double x = wy.getX() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosX();
				double y = 2.0 + wy.getY() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosY();
				double z = wy.getZ() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosZ();
				
				if(distance > renderDistance) {
					x = x / distance * renderDistance;
					y = y / distance * renderDistance;
					z = z / distance * renderDistance;
					distance = renderDistance;
				}
				
				float scale = (float) (0.016666668f * (1.0 + distance) * 0.15);
				
                GL11.glPushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.disableDepth();

                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-scale, -scale, scale);
                
                RenderUtils.drawRect((fr.getStringWidth(tagName) / 2), 0, fr.getStringWidth(tagName) + 11, fr.FONT_HEIGHT + 7, ColorUtils.getColorByInt(Integer.MIN_VALUE));
                RenderUtils.drawOutline((fr.getStringWidth(tagName) / 2), 0, fr.getStringWidth(tagName) + 11, fr.FONT_HEIGHT + 7, 2.5F, wy.getColor());
                
                fr.drawString(tagName, (fr.getStringWidth(tagName) / 2) + 6, 4, Color.WHITE.getRGB());
                
                GlStateManager.enableDepth();
                GL11.glPopMatrix();
			}
		}
	}
	
	@EventTarget
	public void onKey(EventKey event) {
		
		if(event.getKeyCode() == keybindSetting.getKeyCode()) {
			mc.displayGuiScreen(new GuiWaypoint());
		}
	}
	
    private double getDistance(Waypoint wy, Entity entity) {
    	
        double x = wy.getX() - entity.posX;
        double y = wy.getY() - entity.posY;
        double z = wy.getZ() - entity.posZ;
        
        return Math.sqrt(x * x + y * y + z * z);
    }
}
