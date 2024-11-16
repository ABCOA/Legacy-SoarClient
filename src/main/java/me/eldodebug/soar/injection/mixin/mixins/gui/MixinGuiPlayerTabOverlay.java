package me.eldodebug.soar.injection.mixin.mixins.gui;

import java.awt.Color;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.eldodebug.soar.management.mods.impl.TabEditorMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {
	
	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
	public int renderSoarIcon(FontRenderer instance, String text, float x, float y, int color) {
		
		int i = instance.drawStringWithShadow(text, x, y, color);
		
		return i;
	}
	
	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getPlayerEntityByUUID(Ljava/util/UUID;)Lnet/minecraft/entity/player/EntityPlayer;"))
	public EntityPlayer removePlayerHead(WorldClient instance, UUID uuid) {
		
		if(TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getHeadSetting().isToggled()) {
			return null;
		}

		return instance.getPlayerEntityByUUID(uuid);
	}

	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
	public boolean removePlayerHead(Minecraft instance) {
		return instance.isIntegratedServerRunning() && showHeads();
	}

	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;getIsencrypted()Z"))
	public boolean removePlayerHead(NetworkManager instance) {
		return instance.getIsencrypted() && showHeads();
	}
	
	@ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = Integer.MIN_VALUE))
	public int removeBackground(int original) {
		
		if(TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getBackgroundSetting().isToggled()) {
			return new Color(0, 0, 0, 0).getRGB();
		}

		return original;
	}

	@ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 553648127))
	public int removeBackground2(int original) {
		
		if(TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getBackgroundSetting().isToggled()) {
			return new Color(0, 0, 0, 0).getRGB();
		}

		return original;
	}
	
	private boolean showHeads() {
		return !(TabEditorMod.getInstance().isToggled() && !TabEditorMod.getInstance().getHeadSetting().isToggled());
	}
}