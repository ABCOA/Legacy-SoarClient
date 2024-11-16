package me.eldodebug.soar.injection.mixin.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.event.impl.EventMotionUpdate;
import me.eldodebug.soar.management.event.impl.EventSendChat;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

	@Inject(method = "onUpdate", at = @At("HEAD"))
	public void preOnUpdate(CallbackInfo ci) {
		new EventUpdate().call();
	}
	
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void preSendChatMessage(String message, CallbackInfo ci) {
		
		EventSendChat event = new EventSendChat(message);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
	@Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
	private void preOnUpdateWalkingPlayer(CallbackInfo ci) {
		new EventMotionUpdate().call();
	}
}
