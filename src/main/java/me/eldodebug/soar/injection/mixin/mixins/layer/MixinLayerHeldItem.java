package me.eldodebug.soar.injection.mixin.mixins.layer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import me.eldodebug.soar.management.mods.impl.OldAnimationsMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@Mixin(LayerHeldItem.class)
public abstract class MixinLayerHeldItem {

    @Mutable
    @Final
    @Shadow
    private final RendererLivingEntity<?> livingEntityRenderer;

    public MixinLayerHeldItem(RendererLivingEntity<?> rendererLivingEntity) {
        this.livingEntityRenderer = rendererLivingEntity;
    }

    @Overwrite
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float f, float g, float partialTicks, float h, float i, float j, float scale) {
    	
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
    	BooleanSetting sneak = mod.getSneakSetting();
        ItemStack itemStack = entitylivingbaseIn.getHeldItem();
        
        if (itemStack != null) {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild) {
                float k = 0.5F;
                GlStateManager.translate(0.0F, 0.625F, 0.0F);
                GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.scale(k, k, k);
            }

            if (mod.isToggled() && sneak.isToggled() && entitylivingbaseIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }

            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
            
            if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer) entitylivingbaseIn).fishEntity != null) {
                itemStack = new ItemStack(Items.fishing_rod, 0);
            }

            Minecraft minecraft = Minecraft.getMinecraft();

            if ((!mod.isToggled() || !sneak.isToggled()) && entitylivingbaseIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }

            if (mod.isToggled() && mod.getBlockHitSetting().isToggled()) {
            	
                AbstractClientPlayer player = null;
                if (entitylivingbaseIn instanceof AbstractClientPlayer) {
                    player = (AbstractClientPlayer) entitylivingbaseIn;
                }
                EnumAction var26;
                if (player!= null && player.getItemInUseCount() > 0) {
                    var26 = itemStack.getItemUseAction();
                    if (var26 == EnumAction.BLOCK) {
                        GlStateManager.translate(0.05F, 0.0F, -0.1F);
                        GlStateManager.rotate(-50.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(-10.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(-60.0F, 0.0F, 0.0F, 1.0F);
                    }
                }

            }
            
            Item item = itemStack.getItem();
            
            if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
                GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                float f1 = 0.375F;
                GlStateManager.scale(-f1, -f1, f1);
            }

            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemStack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            
            GlStateManager.popMatrix();
        }
    }
}
