package me.eldodebug.soar.injection.mixin.mixins.tileentity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.world.World;

@Mixin(TileEntityBannerRenderer.class)
public class MixinTileEntityBannerRenderer {

    @Redirect(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityBanner;DDDFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTotalWorldTime()J"))
    private long resolveOverflow(World world) {
        return world.getTotalWorldTime() % 100L;
    }
}
