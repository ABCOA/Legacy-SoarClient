package me.eldodebug.soar.injection.mixin.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.ICachedHashcode;
import net.minecraft.block.properties.PropertyHelper;

@Mixin(PropertyHelper.class)
public class MixinPropertyHelper implements ICachedHashcode {

    @Unique
    private int cachedHashcode;

    @Inject(method= "<init>", at= @At("RETURN"))
    private void cacheHashcode(String p_i45652_1_, Class<?> p_i45652_2_, CallbackInfo ci) {
        this.cachedHashcode = this.hashCode();
    }

    @Overwrite
    public int hashCode() {
        return this.cachedHashcode;
    }

    @Override
    public int getCachedHashcode() {
        return this.cachedHashcode;
    }
}