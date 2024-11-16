package me.eldodebug.soar.injection.mixin.mixins.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;
import me.eldodebug.soar.management.event.impl.EventDamageEntity;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.ClientSpooferMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S19PacketEntityStatus;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

	@Shadow
    private Minecraft gameController;
	
	@Shadow
	private WorldClient clientWorldController;
	
	@Shadow
    private NetworkManager netManager;
	
	@Redirect(method = "handleJoinGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
	public void sendBrand() {
		
		PacketBuffer data = new PacketBuffer(Unpooled.buffer()).writeString("Soar Client");
		
		if(ClientSpooferMod.getInstance().isToggled()) {
			
			ComboSetting setting = ClientSpooferMod.getInstance().getTypeSetting();
        	Option type = setting.getOption();
        	
        	if(type.getTranslate().equals(TranslateText.VANILLA)) {
        		data = new PacketBuffer(Unpooled.buffer()).writeString(ClientBrandRetriever.getClientModName());
        	} else if(type.getTranslate().equals(TranslateText.FORGE)) {
        		data = new PacketBuffer(Unpooled.buffer()).writeString("FML");
        	}
		}
		
		netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", data));
	}
	
	@Inject(method = "handleEntityStatus", at = @At("RETURN"))
	public void postHandleEntityStatus(S19PacketEntityStatus packetIn, CallbackInfo callback) {
		if(packetIn.getOpCode() == 2) {
			new EventDamageEntity(packetIn.getEntity(clientWorldController)).call();
		}
	}
}