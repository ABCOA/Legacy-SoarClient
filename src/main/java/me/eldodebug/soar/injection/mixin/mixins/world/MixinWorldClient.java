package me.eldodebug.soar.injection.mixin.mixins.world;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.event.impl.EventLeaveServer;
import me.eldodebug.soar.management.event.impl.EventPlaySound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;

@Mixin(WorldClient.class)
public class MixinWorldClient {

	@Shadow 
	@Final
	private Minecraft mc;
    
	@Inject(method = "sendQuittingDisconnectingPacket", at = @At("HEAD"))
    public void onLeaveServer(CallbackInfo ci) {
		new EventLeaveServer().call();
	}
	
	@Inject(method = "playSound", at = @At(value = "HEAD"), cancellable = true)
	public void handlePlaySound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay, CallbackInfo ci) {
		
		EventPlaySound event = new EventPlaySound(soundName, volume, pitch, volume, pitch);
		event.call();
		
		if(event.getPitch() != event.getOriginalPitch() || event.getVolume() != event.getOriginalVolume()) {
			ci.cancel();
			volume = event.getVolume();
			pitch = event.getPitch();
			double distanceSq = mc.getRenderViewEntity().getDistanceSq(x, y, z);
			
			PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float) x, (float) y, (float) z);

			if(distanceDelay && distanceSq > 100.0D) {
				mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int) (Math.sqrt(distanceSq) / 40.0D * 20.0D));
			} else {
				mc.getSoundHandler().playSound(positionedsoundrecord);
			}
		}
	}
}
