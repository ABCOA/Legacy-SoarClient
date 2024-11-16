package me.eldodebug.soar.injection.mixin.mixins.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.management.mods.impl.FPSBoostMod;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(ChunkRenderDispatcher.class)
public class MixinChunkRenderDispatcher {
	
    @Inject(method = "getNextChunkUpdate", at = @At("HEAD"))
    private void limitChunkUpdates(final CallbackInfoReturnable<ChunkCompileTaskGenerator> cir) {
    	
    	FPSBoostMod mod = FPSBoostMod.getInstance();
    	
    	if(mod != null && mod.isToggled() && mod.getChunkDelaySetting().isToggled()) {
			try {
				Thread.sleep(mod.getDelaySetting().getValueLong() * 15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
}
