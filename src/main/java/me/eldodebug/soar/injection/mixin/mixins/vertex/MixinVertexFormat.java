package me.eldodebug.soar.injection.mixin.mixins.vertex;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

@Mixin(VertexFormat.class)
public class MixinVertexFormat {

    @Shadow
    @Final
    private List<VertexFormatElement> elements;
    
    @Shadow
    @Final
    private List<Integer> offsets;
    
    @Shadow
    private int nextOffset;
    
    @Unique
    private int cachedHashCode;

    @Inject(method = "addElement", at = @At(value="RETURN", ordinal=1))
    private void resetHashCode(VertexFormatElement element, CallbackInfoReturnable<VertexFormat> cir) {
        this.cachedHashCode = 0;
    }

    @Overwrite(remap=false)
    public int hashCode() {
    	
        if (this.cachedHashCode != 0) {
            return this.cachedHashCode;
        }
        
        int i = this.elements.hashCode();
        
        i = 31 * i + this.offsets.hashCode();
        this.cachedHashCode = i = 31 * i + this.nextOffset;
        
        return i;
    }
}
