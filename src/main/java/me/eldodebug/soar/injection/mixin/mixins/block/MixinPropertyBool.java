package me.eldodebug.soar.injection.mixin.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.properties.PropertyBool;

@Mixin(PropertyBool.class)
public class MixinPropertyBool {

    @Unique
    private static final ImmutableSet<Boolean> ALLOWED_VALUES = ImmutableSet.of(Boolean.valueOf(true), Boolean.valueOf(false));

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;", remap=false))
    private ImmutableSet<Boolean> useCached(Object first, Object second) {
        return ALLOWED_VALUES;
    }
}
