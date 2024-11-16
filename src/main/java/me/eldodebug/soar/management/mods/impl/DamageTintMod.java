package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRenderDamageTint;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.utils.PlayerUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class DamageTintMod extends Mod {

	private ResourceLocation shape = new ResourceLocation("soar/shape.png");
    private SimpleAnimation animation = new SimpleAnimation(0.0F);
    
    private NumberSetting healthSetting = new NumberSetting(TranslateText.HEALTH, this, 10, 5, 16, true);
    
	public DamageTintMod() {
		super(TranslateText.DAMAGE_TINT, TranslateText.DAMAGE_TINT_DESCRIPTION, ModCategory.RENDER);
	}
	
	@EventTarget
	public void onRenderDamageTint(EventRenderDamageTint event) {
		
		float threshold = healthSetting.getValueFloat();

		ScaledResolution sr = new ScaledResolution(mc);
		
		if(PlayerUtils.isCreative() || mc.thePlayer.isSpectator()) {
			return;
		}
		
		animation.setAnimation(mc.thePlayer.getHealth() <= threshold ? 1.0F : 0.0F, 10);
		
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0);

        GlStateManager.color(0F, animation.getValue(), animation.getValue(), animation.getValue());
        mc.getTextureManager().bindTexture(shape);
        Tessellator tes = Tessellator.getInstance();
        WorldRenderer wr = tes.getWorldRenderer();
        
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0.0D, sr.getScaledHeight_double(), -90.0D).tex(0.0D, 1.0D).endVertex();
        wr.pos(sr.getScaledWidth_double(), sr.getScaledHeight_double(), -90.0D).tex(1.0D, 1.0D).endVertex();
        wr.pos(sr.getScaledWidth_double(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        wr.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tes.draw();
        
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
	}
}
