package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.badge.Badge;
import me.eldodebug.soar.management.mods.impl.TabEditorMod;
import me.eldodebug.soar.utils.render.IconFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;
import java.util.UUID;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay {

	private NetworkPlayerInfo activeBadgePlayerInfo;

	@Shadow
	public abstract String getPlayerName(NetworkPlayerInfo networkPlayerInfo);

	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;getPlayerName(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;", ordinal = 0))
	public String measureBadgePlayerName(GuiPlayerTabOverlay instance, NetworkPlayerInfo networkPlayerInfo) {
		return getPlayerNameWithBadgeSpace(instance.getPlayerName(networkPlayerInfo), networkPlayerInfo);
	}

	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;getPlayerName(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;", ordinal = 1))
	public String renderBadgePlayerName(GuiPlayerTabOverlay instance, NetworkPlayerInfo networkPlayerInfo) {
		activeBadgePlayerInfo = networkPlayerInfo;
		return getPlayerNameWithBadgeSpace(instance.getPlayerName(networkPlayerInfo), networkPlayerInfo);
	}
	
	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 1))
	public int renderSoarIconForSpectator(FontRenderer instance, String text, float x, float y, int color) {
		return renderPlayerNameWithBadge(instance, text, x, y, color);
	}

	@Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
	public int renderSoarIcon(FontRenderer instance, String text, float x, float y, int color) {
		return renderPlayerNameWithBadge(instance, text, x, y, color);
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

	private String getPlayerNameWithBadgeSpace(String playerName, NetworkPlayerInfo networkPlayerInfo) {
		return getBadge(networkPlayerInfo) == null ? playerName : "  " + playerName;
	}

	private int renderPlayerNameWithBadge(FontRenderer fontRenderer, String text, float x, float y, int color) {
		NetworkPlayerInfo playerInfo = activeBadgePlayerInfo;
		activeBadgePlayerInfo = null;

		Badge badge = getBadge(playerInfo);

		if(badge != null) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			IconFontRenderer.drawIcon(badge.getIcon(), x, y, 8.0F, badge.getColor());
		}

		return fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	private Badge getBadge(NetworkPlayerInfo networkPlayerInfo) {
		if(networkPlayerInfo == null || Soar.getInstance().getBadgeManager() == null) {
			return null;
		}

		return Soar.getInstance().getBadgeManager().getBadge(networkPlayerInfo.getGameProfile());
	}
}
