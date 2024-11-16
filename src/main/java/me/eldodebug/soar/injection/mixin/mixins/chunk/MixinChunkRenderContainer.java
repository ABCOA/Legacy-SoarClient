package me.eldodebug.soar.injection.mixin.mixins.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.event.impl.EventPreRenderChunk;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.chunk.RenderChunk;

@Mixin(ChunkRenderContainer.class)
public class MixinChunkRenderContainer {

	@Inject(method = "preRenderChunk", at = @At("RETURN"))
	public void preRenderChunk(RenderChunk renderChunkIn, CallbackInfo callback) {
		new EventPreRenderChunk(renderChunkIn).call();
	}
}
