package me.eldodebug.soar.injection.mixin.mixins.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem {

	@Shadow
    public abstract ItemStack getEntityItem();
	
	@Inject(method = "searchForOtherItemsNearby", at = @At("HEAD"), cancellable = true)
	private void combineFix(CallbackInfo ci) {
		if (this.getEntityItem().stackSize >= this.getEntityItem().getMaxStackSize()) {
			ci.cancel();
		}
	}
}
