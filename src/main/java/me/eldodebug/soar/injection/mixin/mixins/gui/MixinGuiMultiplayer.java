package me.eldodebug.soar.injection.mixin.mixins.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.gui.GuiFixConnecting;
import me.eldodebug.soar.management.mods.impl.ViaVersionMod;
import me.eldodebug.soar.viaversion.ViaSoar;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;

@Mixin(GuiMultiplayer.class)
public class MixinGuiMultiplayer extends GuiScreen {

	@Inject(method = "initGui", at = @At("TAIL"))
    public void preInitGui(CallbackInfo ci) {
		if(ViaVersionMod.getInstance().isToggled()) {
			this.buttonList.add(ViaSoar.getInstance().getAsyncVersionSlider());
		}
	}
	
	@Overwrite
    private void connectToServer(ServerData server){
        mc.displayGuiScreen(new GuiFixConnecting(this, mc, server));
    }
}
