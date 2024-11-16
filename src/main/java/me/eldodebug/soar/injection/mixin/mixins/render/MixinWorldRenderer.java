package me.eldodebug.soar.injection.mixin.mixins.render;

import java.nio.IntBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

	@Shadow
    private boolean isDrawing;
	
    @Shadow 
    private IntBuffer rawIntBuffer;
    
    @Shadow 
    private VertexFormat vertexFormat;
    
    @Inject(method = "finishDrawing", at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;limit(I)Ljava/nio/Buffer;", remap = false))
    private void resetBuffer(CallbackInfo ci) {
        this.rawIntBuffer.position(0);
    }

    @Inject(method = "endVertex", at = @At("HEAD"))
    private void adjustBuffer(CallbackInfo ci) {
        this.rawIntBuffer.position(this.rawIntBuffer.position() + this.vertexFormat.getIntegerSize());
    }
}