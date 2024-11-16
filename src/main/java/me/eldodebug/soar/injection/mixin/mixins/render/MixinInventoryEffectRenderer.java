package me.eldodebug.soar.injection.mixin.mixins.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.eldodebug.soar.management.mods.impl.InventoryMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends GuiContainer {

	public MixinInventoryEffectRenderer(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Redirect(method = "updateActivePotionEffects", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;guiLeft:I", ordinal = 0))
	public void preventPotionShift(InventoryEffectRenderer instance, int value) {
		
		if(InventoryMod.getInstance().isToggled() && InventoryMod.getInstance().getPreventPotionShiftSetting().isToggled()) {
			guiLeft = (width - xSize) / 2;
			return;
		}

		guiLeft = value;
	}
}
