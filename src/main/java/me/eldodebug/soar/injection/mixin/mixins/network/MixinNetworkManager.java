package me.eldodebug.soar.injection.mixin.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.eldodebug.soar.hooks.OnlineChatMetadata;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.event.impl.EventSendPacket;
import me.eldodebug.soar.management.online.LocationCollector;
import me.eldodebug.soar.management.online.PresenceManager;
import me.eldodebug.soar.viaversion.netty.event.CompressionReorderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
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
        if (packet instanceof C01PacketChatMessage) {
            final Minecraft mc = Minecraft.getMinecraft();
            final NetworkManager source = (NetworkManager)(Object)this;
            final String text = ((C01PacketChatMessage)packet).getMessage();
            Runnable observe = () -> {
                if (mc.getNetHandler() != null && mc.getNetHandler().getNetworkManager() == source)
                    LocationCollector.getInstance().observeOutgoingChat(text);
            };
            if (mc.isCallingFromMinecraftThread()) observe.run(); else mc.addScheduledTask(observe);
        }
    }
    
	@Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void preChannelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        final Minecraft mc = Minecraft.getMinecraft();
        final NetworkManager source = (NetworkManager)(Object)this;
        if (packet instanceof S02PacketChat) {
            final S02PacketChat chat = (S02PacketChat)packet;
            // EventReceivePacket listeners may decorate this component with copy/translation buttons.
            final String rawText = chat.getChatComponent().getUnformattedText();
            final byte chatType = chat.getType();
            mc.addScheduledTask(() -> {
                if (mc.getNetHandler() == null || mc.getNetHandler().getNetworkManager() != source) return;
                PresenceManager online = PresenceManager.getInstance();
                if (chatType != 2 && online != null && online.onGameChat(rawText))
                    OnlineChatMetadata.suppress(chat.getChatComponent());
                else OnlineChatMetadata.record(chat.getChatComponent(), chat.getType());
            });
        } else if (packet instanceof S3FPacketCustomPayload) {
            final S3FPacketCustomPayload payload = (S3FPacketCustomPayload)packet;
            final String identifier = payload.getChannelName();
            if (("hypixel:hello".equals(identifier) || "hyevent:location".equals(identifier))
                    && payload.getBufferData().readableBytes() <= 32767) {
                // Vanilla releases this buffer after processing; copy without advancing its reader index.
                final byte[] bytes = new byte[payload.getBufferData().readableBytes()];
                payload.getBufferData().getBytes(payload.getBufferData().readerIndex(), bytes);
                mc.addScheduledTask(() -> {
                    if (mc.getNetHandler() == null || mc.getNetHandler().getNetworkManager() != source) return;
                    PresenceManager online = PresenceManager.getInstance();
                    if (online != null) online.onCustomPayload(identifier, bytes);
                });
            }
        }
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
