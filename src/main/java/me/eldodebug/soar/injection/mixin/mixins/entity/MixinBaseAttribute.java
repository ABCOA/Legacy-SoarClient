package me.eldodebug.soar.injection.mixin.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.attributes.BaseAttribute;
import net.minecraft.entity.ai.attributes.IAttribute;

@Mixin(BaseAttribute.class)
public class MixinBaseAttribute {

    @Unique
    private int cachedHashcode;

    @Inject(method="<init>", at=@At("RETURN"))
    private void cacheHashcode(IAttribute i, String s, double d1, CallbackInfo ci) {
        this.cachedHashcode = this.hashCode();
    }

    @Overwrite
    public int hashCode() {
        return this.cachedHashcode;
    }
}
