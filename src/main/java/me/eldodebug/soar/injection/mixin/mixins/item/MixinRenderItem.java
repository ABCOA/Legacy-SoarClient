package me.eldodebug.soar.injection.mixin.mixins.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.mods.impl.GlintColorMod;
import me.eldodebug.soar.management.mods.impl.ShinyPotsMod;
import me.eldodebug.soar.utils.EnumFacings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

	@Shadow
	public abstract void renderModel(IBakedModel model, int color);
	
	@Shadow
    private TextureManager textureManager;
	
	@Overwrite
    private void renderEffect(IBakedModel model){
		
        int color = -8372020; 
        
        if(GlintColorMod.getInstance().isToggled()) {
        	color = GlintColorMod.getInstance().getGlintColor().getRGB();
        }
        
        GlStateManager.depthMask(false);
        
        if(!ShinyPotsMod.getInstance().isToggled()) {
            GlStateManager.depthFunc(514);
        }
        
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(768, 1);
        this.textureManager.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, color);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, color);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableLighting();
        
        if(!ShinyPotsMod.getInstance().isToggled()) {
            GlStateManager.depthFunc(515);
        }
        
        GlStateManager.depthMask(true);
        this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
    }
	
	@Inject(method = "renderItemAndEffectIntoGUI", at = @At("HEAD"))
	public void preRenderItemAndEffectIntoGUIhead(final ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
        GlStateManager.enableDepth();
	}
	
	@Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"))
	public void preRenderItemOverlayIntoGUIhead(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        GlStateManager.enableDepth();
	}
	
    @Redirect(method = "renderModel(Lnet/minecraft/client/resources/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] renderModel$getCachedArray() {
        return EnumFacings.FACINGS;
    }
}
