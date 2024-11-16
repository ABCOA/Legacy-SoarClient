package me.eldodebug.soar.injection.mixin.mixins.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.event.impl.EventRenderTNT;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.item.EntityTNTPrimed;

@Mixin(RenderTNTPrimed.class)
public class MixinRenderTNTPrimed {

	@Inject(method = "doRender", at = @At("HEAD"))
	public void preDoRender(EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		new EventRenderTNT((RenderTNTPrimed) (Object) this, entity, x, y, z, entityYaw, partialTicks).call();
	}
}
