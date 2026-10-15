package me.eldodebug.soar.injection.mixin.mixins.entity;

import me.eldodebug.soar.management.event.impl.EventFovUpdate;
import me.eldodebug.soar.management.event.impl.EventLocationCape;
import me.eldodebug.soar.management.event.impl.EventLocationSkin;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

	@Shadow
    private NetworkPlayerInfo playerInfo;

	@Shadow
	protected NetworkPlayerInfo getPlayerInfo() { return null; }
	
	@Inject(method = "getFovModifier", at = @At("RETURN"), cancellable = true)
    public void getFovModifier(CallbackInfoReturnable<Float> cir) {
		
        EventFovUpdate event = new EventFovUpdate((AbstractClientPlayer) (Object)this, cir.getReturnValue());
        event.call();
        
        cir.setReturnValue(event.getFov());
    }
	
	@Inject(method = "getLocationSkin", at = @At("HEAD"), cancellable = true)
	public void onGetLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
		
		EventLocationSkin event = new EventLocationSkin(playerInfo);
		event.call();
		
		if(event.isCancelled()) {
			cir.cancel();
			cir.setReturnValue(event.getSkin());
		}
	}
	
	@Inject(method = "getLocationCape", cancellable = true, at = @At("HEAD"))
	public void onGetLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
		
		// Initialize the lazy player info before the event; direct field access may be null on the first render.
		EventLocationCape event = new EventLocationCape((AbstractClientPlayer) (Object) this, getPlayerInfo());
		event.call();
		
		if(event.isCancelled()) {
			cir.cancel();
			cir.setReturnValue(event.getCape());
		}
	}
}
