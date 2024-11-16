package me.eldodebug.soar.injection.mixin.mixins.model;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.eldodebug.soar.utils.EnumFacings;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.util.EnumFacing;

@Mixin(FaceBakery.class)
public class MixinFaceBakery {

    @Redirect(method = "getPositionsDiv16", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] getPositionsDiv16$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "getFacingFromVertexData", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private static EnumFacing[] getFacingFromVertexData$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "applyFacing", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] applyFacing$getCachedArray() {
        return EnumFacings.FACINGS;
    }
}
