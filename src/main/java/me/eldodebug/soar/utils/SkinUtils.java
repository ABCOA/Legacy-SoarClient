package me.eldodebug.soar.utils;

import me.eldodebug.soar.injection.interfaces.IMixinEntityPlayer;
import me.eldodebug.soar.management.mods.impl.skin3d.opengl.NativeImage;
import me.eldodebug.soar.management.mods.impl.skin3d.render.CustomizableModelPart;
import me.eldodebug.soar.management.mods.impl.skin3d.render.SolidPixelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

public class SkinUtils {
	
    public static boolean hasCustomSkin(AbstractClientPlayer player) {
        return !DefaultPlayerSkin.getDefaultSkin((player).getUniqueID()).equals((player).getLocationSkin());
    }

    private static NativeImage getSkinTexture(AbstractClientPlayer player) {
        return getTexture(player.getLocationSkin());
    }
    
    private static NativeImage getTexture(ResourceLocation resource) {
    	
        NativeImage skin = new NativeImage(64, 64, false);
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject abstractTexture = textureManager.getTexture(resource);
        
        if(abstractTexture == null) {
        	return null;
        }
        
        GlStateManager.bindTexture(abstractTexture.getGlTextureId());
        skin.downloadTexture(0, false);
        return skin;
    }
    
    public static boolean setup3dLayers(AbstractClientPlayer abstractClientPlayerEntity, IMixinEntityPlayer settings, boolean thinArms, ModelPlayer model) {
    	
        if(!hasCustomSkin(abstractClientPlayerEntity)) {
            return false;
        }
        
        NativeImage skin = getSkinTexture(abstractClientPlayerEntity);
        
        if(skin == null) {
        	return false;
        }
        
        CustomizableModelPart[] layers = new CustomizableModelPart[5];
        layers[0] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 48, true, 0f);
        layers[1] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 32, true, 0f);
        
        if(thinArms) {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 48, 48, true, -2.5f);
            layers[3] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 40, 32, true, -2.5f);
        } else {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 48, 48, true, -2.5f);
            layers[3] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 40, 32, true, -2.5f);
        }
        
        layers[4] = SolidPixelWrapper.wrapBox(skin, 8, 12, 4, 16, 32, true, -0.8f);
        settings.setupSkinLayers(layers);
        settings.setupHeadLayers(SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f));
        skin.close();
        
        return true;
    }
}
