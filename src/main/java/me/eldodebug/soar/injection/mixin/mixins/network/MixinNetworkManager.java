package me.eldodebug.soar.injection.mixin.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.event.impl.EventSendPacket;
import me.eldodebug.soar.viaversion.netty.event.CompressionReorderEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

	@Shadow
	private Channel channel;
	
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void preSendPacket(Packet<?> packet, CallbackInfo ci) {
    	
	   	EventSendPacket event = new EventSendPacket(packet);
    	event.call();
    	
    	if(event.isCancelled()) {
    		ci.cancel();
            return;
    	}

    }
    
	@Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void preChannelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
		EventReceivePacket event = new EventReceivePacket(packet);
		event.call();
		
		if(event.isCancelled()) {
			ci.cancel();
		}
	}
	
	@Inject(method = "setCompressionTreshold", at = @At("TAIL"))
    public void setUserEvent(int treshold, CallbackInfo ci) {
		this.channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());
    }
}
