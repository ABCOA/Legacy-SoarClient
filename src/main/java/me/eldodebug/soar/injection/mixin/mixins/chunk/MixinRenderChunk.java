package me.eldodebug.soar.injection.mixin.mixins.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.event.impl.EventRenderChunkPosition;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;

@Mixin(RenderChunk.class)
public class MixinRenderChunk {

	@Inject(method = "setPosition", at = @At("RETURN"))
	public void setPosition(BlockPos pos, CallbackInfo ci) {
		new EventRenderChunkPosition((RenderChunk) (Object) this, pos).call();
	}
}
