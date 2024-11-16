package me.eldodebug.soar.management.mods.impl.skin3d.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import me.eldodebug.soar.injection.interfaces.IMixinEntityPlayer;
import me.eldodebug.soar.injection.interfaces.IMixinRenderPlayer;
import me.eldodebug.soar.management.mods.impl.Skin3DMod;
import me.eldodebug.soar.management.mods.impl.skin3d.render.CustomizableModelPart;
import me.eldodebug.soar.utils.SkinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;

public class BodyLayerFeatureRenderer implements LayerRenderer<AbstractClientPlayer> {
    
    private final boolean thinArms;
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public BodyLayerFeatureRenderer(RenderPlayer playerRenderer) {
        thinArms = ((IMixinRenderPlayer)playerRenderer).hasThinArms();
        bodyLayers.add(new Layer(0, false, EnumPlayerModelParts.LEFT_PANTS_LEG, Shape.LEGS, () -> playerRenderer.getMainModel().bipedLeftLeg));
        bodyLayers.add(new Layer(1, false, EnumPlayerModelParts.RIGHT_PANTS_LEG, Shape.LEGS, () -> playerRenderer.getMainModel().bipedRightLeg));
        bodyLayers.add(new Layer(2, false, EnumPlayerModelParts.LEFT_SLEEVE, thinArms ? Shape.ARMS_SLIM : Shape.ARMS, () -> playerRenderer.getMainModel().bipedLeftArm));
        bodyLayers.add(new Layer(3, true, EnumPlayerModelParts.RIGHT_SLEEVE, thinArms ? Shape.ARMS_SLIM : Shape.ARMS, () -> playerRenderer.getMainModel().bipedRightArm));
        bodyLayers.add(new Layer(4, false, EnumPlayerModelParts.JACKET, Shape.BODY, () -> playerRenderer.getMainModel().bipedBody));
    }
    
    @Override
    public void doRenderLayer(AbstractClientPlayer player, float paramFloat1, float paramFloat2, float paramFloat3, float deltaTick, float paramFloat5, float paramFloat6, float paramFloat7) {
    	
        if (!player.hasSkin() || player.isInvisible()) {
            return;
        }
        
        if(mc.theWorld == null) {
            return;
        }
        
        if(mc.thePlayer.getPositionVector().squareDistanceTo(player.getPositionVector()) > Skin3DMod.getInstance().getRenderDistanceLOD()) {
        	return;
        }
        
        IMixinEntityPlayer settings = (IMixinEntityPlayer) player;
        
        if(settings.getSkinLayers() == null && !setupModel(player, settings)) {
            return;
        }

        renderLayers(player, (CustomizableModelPart[]) settings.getSkinLayers(), deltaTick);
    }

    private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, IMixinEntityPlayer settings) {
    	
        if(!SkinUtils.hasCustomSkin(abstractClientPlayerEntity)) {
            return false;
        }
        
        SkinUtils.setup3dLayers(abstractClientPlayerEntity, settings, thinArms, null);
        
        return true;
    }
    
    private final List<Layer> bodyLayers = new ArrayList<>();
    
    class Layer{
    	
        int layersId;
        boolean mirrored;
        EnumPlayerModelParts modelPart;
        Shape shape;
        Supplier<ModelRenderer> vanillaGetter;
        
        public Layer(int layersId, boolean mirrored, EnumPlayerModelParts modelPart, Shape shape, Supplier<ModelRenderer> vanillaGetter) {
            this.layersId = layersId;
            this.mirrored = mirrored;
            this.modelPart = modelPart;
            this.shape = shape;
            this.vanillaGetter = vanillaGetter;
        }
    }
    
    private enum Shape {
        HEAD(0), BODY(0.6f), LEGS(-0.2f), ARMS(0.4f), ARMS_SLIM(0.4f);
        
        private final float yOffsetMagicValue;

        private Shape(float yOffsetMagicValue) {
            this.yOffsetMagicValue = yOffsetMagicValue;
        }
    }
    
    public void renderLayers(AbstractClientPlayer abstractClientPlayer, CustomizableModelPart[] layers, float deltaTick) {
    	
        if(layers == null) {
        	return;
        }
        
        float pixelScaling = Skin3DMod.getInstance().getBaseVoxelSize();
        float heightScaling = 1.035f;
        float widthScaling = Skin3DMod.getInstance().getBaseVoxelSize();
        
        boolean redTint = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0;
        
        for(Layer layer : bodyLayers) {
            if(abstractClientPlayer.isWearing(layer.modelPart) && !layer.vanillaGetter.get().isHidden) {
            	
                GlStateManager.pushMatrix();
                
                if(abstractClientPlayer.isSneaking()) {
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
                }
                
                layer.vanillaGetter.get().postRender(0.0625F);
                
                if(layer.shape == Shape.ARMS) {
                    layers[layer.layersId].x = 0.998f*16;
                } else if(layer.shape == Shape.ARMS_SLIM) {
                    layers[layer.layersId].x = 0.499f*16;
                }
                
                if(layer.shape == Shape.BODY) {
                    widthScaling = Skin3DMod.getInstance().getBodyVoxelWidthSize();
                }else {
                    widthScaling = Skin3DMod.getInstance().getBaseVoxelSize();
                }
                
                if(layer.mirrored) {
                    layers[layer.layersId].x *= -1;
                }
                GlStateManager.scale(0.0625, 0.0625, 0.0625);
                GlStateManager.scale(widthScaling, heightScaling, pixelScaling);
                layers[layer.layersId].y = layer.shape.yOffsetMagicValue;
                
                layers[layer.layersId].render(redTint);
                GlStateManager.popMatrix();
            }
        }
        
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }   
}