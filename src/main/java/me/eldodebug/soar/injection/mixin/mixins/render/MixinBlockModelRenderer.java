package me.eldodebug.soar.injection.mixin.mixins.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.eldodebug.soar.utils.EnumFacings;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.EnumFacing;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    @Redirect(method = "renderModelAmbientOcclusion", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] ambientOcclusion$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "renderModelStandard", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] standard$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "fillQuadBounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] fillQuadBounds$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "renderModelBrightnessColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] renderModelBrightnessColor$getCachedArray() {
        return EnumFacings.FACINGS;
    }
}
