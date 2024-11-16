package me.eldodebug.soar.injection.mixin.mixins.optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.management.event.impl.EventGamma;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@Pseudo
@Mixin(targets = "net.optifine.LightMap")
public class MixinLightMap {

	@Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
	public void overrideGamma(World world, float torchFlickerX, int[] lmColors, boolean nightVision, CallbackInfoReturnable<Boolean> callback) {

		EventGamma event = new EventGamma(Minecraft.getMinecraft().gameSettings.gammaSetting);
		event.call();

		if(event.getGamma() > 1) {
			callback.setReturnValue(false);
		}
	}
}
