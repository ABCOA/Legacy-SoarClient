package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventRenderExpBar;
import me.eldodebug.soar.management.event.impl.EventRenderTooltip;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModernHotbarMod extends HUDMod {

	private SimpleAnimation animation = new SimpleAnimation(0.0F);
	
	private float barX, barY, barWidth, barHeight, selX;
	
	private ComboSetting designSetting = new ComboSetting(TranslateText.DESIGN, this, TranslateText.NORMAL, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.NORMAL), new Option(TranslateText.SOAR), new Option(TranslateText.CHILL))));
	
	private BooleanSetting smoothSetting = new BooleanSetting(TranslateText.SMOOTH, this, true);
	
	public ModernHotbarMod() {
		super(TranslateText.MODERN_HOTBAR, TranslateText.MODERN_HOTBAR_DESCRIPTION);
		
		this.setDraggable(false);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
		ScaledResolution sr = new ScaledResolution(mc);
		Option option = designSetting.getOption();
		
		nvg.setupAndDraw(() -> drawNanoVG(nvg));
		
        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
        	
            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
            
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int j = 0; j < 9; ++j) {
                int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 16 - 3;
                
            	if(option.getTranslate().equals(TranslateText.CHILL)) {
                	l = l + 4;
                }
                
                renderHotBarItem(j, k, l - 4, event.getPartialTicks(), entityplayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
	}
	
    private void renderHotBarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer entityPlayer) {
    	
        ItemStack itemstack = entityPlayer.inventory.mainInventory[index];
        RenderItem itemRenderer = mc.getRenderItem();

        if (itemstack != null) {
            float f = (float) itemstack.animationsToGo - partialTicks;

            if (f > 0.0F) {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

            if (f > 0.0F) {
                GlStateManager.popMatrix();
            }

            itemRenderer.renderItemOverlays(mc.fontRendererObj, itemstack, xPos, yPos);
        }
    }
    
	private void drawNanoVG(NanoVGManager nvg) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		Option option = designSetting.getOption();
		AccentColor currentColor = Soar.getInstance().getColorManager().getCurrentColor();
		
        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
        	
        	if(!option.getTranslate().equals(TranslateText.CHILL)) {
    			
    			barX = sr.getScaledWidth() / 2.0F - 91;
    			barY = sr.getScaledHeight() - 26;
    			barWidth = 91 * 2;
    			barHeight = 22;
    			
    			if(option.getTranslate().equals(TranslateText.SOAR)) {
    				nvg.drawShadow(barX, barY, barWidth, barHeight, 6);
    				nvg.drawGradientRoundedRect(barX, barY, barWidth, barHeight, 6, ColorUtils.applyAlpha(currentColor.getColor1(), 190), ColorUtils.applyAlpha(currentColor.getColor2(), 190));
    			}else {
    				nvg.drawShadow(barX, barY, barWidth, barHeight, 6);
    				nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6, new Color(0, 0, 0, 100));
    			}
    		}else {
    			
    			barX = 0;
    			barY = sr.getScaledHeight() - 22;
    			barWidth = sr.getScaledWidth();
    			barHeight = 22;
    			
    			nvg.drawShadow(barX, barY, barWidth, barHeight, 0);
    			nvg.drawRect(barX, barY, barWidth, barHeight, new Color(20, 20, 20, 180));
    		}
    		
            EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
            
            int i = sr.getScaledWidth() / 2;

            if (smoothSetting.isToggled()) {
            	animation.setAnimation(i - 91 - 1 + entityplayer.inventory.currentItem * 20, 18);
            	selX = animation.getValue();
            } else {
                selX = i - 91 - 1 + entityplayer.inventory.currentItem * 20;
            }

        	if(!option.getTranslate().equals(TranslateText.CHILL)) {
    			if(option.getTranslate().equals(TranslateText.SOAR)) {
    				nvg.drawRoundedRect(selX + 1, sr.getScaledHeight() - 22 - 4, 22, 22, 6, new Color(255, 255, 255, 140));
        		}else {
        			nvg.drawRoundedRect(selX + 1, sr.getScaledHeight() - 22 - 4, 22, 22, 6, new Color(0, 0, 0, 100));
        		}
    		}else {
    			nvg.drawRect(selX + 1, sr.getScaledHeight() - 22, 22, 22, new Color(230, 230, 230, 180));
    		}
        }
	}
	
	@EventTarget
	public void onRenderTooltip(EventRenderTooltip event) {
		event.setCancelled(true);
	}
	
	@EventTarget
	public void onRenderExpBar(EventRenderExpBar event) {
		
		Option option = designSetting.getOption();
		
		event.setCancelled(!option.getTranslate().equals(TranslateText.CHILL));
	}
}
