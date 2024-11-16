package me.eldodebug.soar.injection.mixin.mixins.render;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.injection.interfaces.IMixinRenderPlayer;
import me.eldodebug.soar.management.event.impl.EventHitOverlay;
import me.eldodebug.soar.management.event.impl.EventRendererLivingEntity;
import me.eldodebug.soar.management.mods.impl.NametagMod;
import me.eldodebug.soar.management.mods.impl.Skin3DMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity <T extends EntityLivingBase> extends Render<T> {

	private float red;
	private float green;
	private float blue;
	private float alpha;
	
    protected MixinRendererLivingEntity(RenderManager renderManager) {
		super(renderManager);
	}

	@Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
	public void preRenderName(T entity, double x, double y, double z, CallbackInfo ci) {

		if (this.canRenderName(entity)) {

			String str = entity.getDisplayName().getFormattedText();

			double d0 = entity.getDistanceSqToEntity(Minecraft.getMinecraft().getRenderManager().livingPlayer);
			float f = entity.isSneaking() ? 32.0F : 64.0F;

			if (d0 < (double)(f * f)) {
				String s = entity.getDisplayName().getFormattedText();
				GlStateManager.alphaFunc(516, 0.1F);

				if (entity.isSneaking()) {
					FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
					GlStateManager.pushMatrix();
					GlStateManager.translate((float)x, (float)y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float)z);
					GL11.glNormal3f(0.0F, 1.0F, 0.0F);
					GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
					GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
					GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
					GlStateManager.translate(0.0F, 9.374999F, 0.0F);
					GlStateManager.disableLighting();
					GlStateManager.depthMask(false);
					GlStateManager.enableBlend();
					GlStateManager.disableTexture2D();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					int i = fontrenderer.getStringWidth(s) / 2;
					Tessellator tessellator = Tessellator.getInstance();
					WorldRenderer worldrenderer = tessellator.getWorldRenderer();
					worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
					worldrenderer.pos((double)(-i - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
					worldrenderer.pos((double)(-i - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
					worldrenderer.pos((double)(i + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
					worldrenderer.pos((double)(i + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
					tessellator.draw();
					GlStateManager.enableTexture2D();
					GlStateManager.depthMask(true);

					fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2 + 0, 0, 553648127);
					GlStateManager.enableLighting();
					GlStateManager.disableBlend();
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.popMatrix();
				} else {
					renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double)(entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, d0);
				}
			}
		}
		ci.cancel();
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
	public void preDoRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		
		EventRendererLivingEntity event = new EventRendererLivingEntity((RendererLivingEntity<EntityLivingBase>) (Object)this, entity, x, y, z);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
	@Redirect(method = "canRenderName", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;livingPlayer:Lnet/minecraft/entity/Entity;"))
	public Entity renderOwnName(RenderManager manager) {
		
		if(NametagMod.getInstance().isToggled()) {
			return null;
		}
		
		return manager.livingPlayer;
	}
	
    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F"))
    private float fixNametagPerspective(RenderManager instance) {
        return instance.playerViewX * Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1;
    }
    
	@Inject(method = "renderModel", at = @At("TAIL"))
    private void renderModelLayers(T p_renderModel_1_, float p_renderModel_2_, float p_renderModel_3_, float p_renderModel_4_, float p_renderModel_5_, float p_renderModel_6_, float p_renderModel_7_, CallbackInfo info) {
    	
    	if(Skin3DMod.getInstance().isToggled()) {
    		
            if(!(this instanceof IMixinRenderPlayer)) {
                return;
            }
            
            boolean flag = !p_renderModel_1_.isInvisible();
            boolean flag1 = (!flag && !p_renderModel_1_.isInvisibleToPlayer((Minecraft.getMinecraft()).thePlayer));
            
            if (flag || flag1) {
            	
            	IMixinRenderPlayer playerRenderer = (IMixinRenderPlayer) this;
            	
                if (flag1) {
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.alphaFunc(516, 0.003921569F);
                }
                playerRenderer.getHeadLayer().doRenderLayer((AbstractClientPlayer) p_renderModel_1_, p_renderModel_2_, 0f, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                playerRenderer.getBodyLayer().doRenderLayer((AbstractClientPlayer) p_renderModel_1_, p_renderModel_2_, 0f, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                if (flag1) {
                    GlStateManager.disableBlend();
                    GlStateManager.alphaFunc(516, 0.1F);
                    GlStateManager.popMatrix();
                    GlStateManager.depthMask(true);
                }
            }
    	}
    }
	
	@Inject(method = "setBrightness", at = @At("HEAD"))
	public void hitColor(T entitylivingbaseIn, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
		
		EventHitOverlay event = new EventHitOverlay(1, 0, 0, 0.3F);
		event.call();

		red = event.getRed();
		green = event.getGreen();
		blue = event.getBlue();
		alpha = event.getAlpha();
	}

	@ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 1, ordinal = 0))
	public float setBrightnessRed(float original) {
		return red;
	}

	@ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0, ordinal = 0))
	public float setBrightnessGreen(float original) {
		return green;
	}

	@ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0, ordinal = 1))
	public float setBrightnessBlue(float original) {
		return blue;
	}

	@ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0.3F, ordinal = 0))
	public float setBrightnessAlpha(float original) {
		return alpha;
	}
}
