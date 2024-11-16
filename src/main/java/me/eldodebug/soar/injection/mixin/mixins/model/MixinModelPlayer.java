package me.eldodebug.soar.injection.mixin.mixins.model;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.mods.impl.WaveyCapesMod;
import net.minecraft.client.model.ModelPlayer;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer {

    @Inject(method = "renderCape", at = @At("HEAD"), cancellable = true)
    public void renderCloak(float p_renderCape_1_, CallbackInfo ci) {
    	if(WaveyCapesMod.getInstance().isToggled()) {
    		ci.cancel();
    	}
    }
}
