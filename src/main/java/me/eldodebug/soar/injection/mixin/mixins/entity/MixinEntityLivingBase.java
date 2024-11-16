package me.eldodebug.soar.injection.mixin.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.injection.interfaces.IMixinEntityLivingBase;
import me.eldodebug.soar.management.event.impl.EventLivingUpdate;
import me.eldodebug.soar.management.mods.impl.SlowSwingMod;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements IMixinEntityLivingBase {

	@Shadow
	public abstract int getArmSwingAnimationEnd();
	
	public MixinEntityLivingBase(World worldIn) {
		super(worldIn);
	}

    @Inject(method = "onEntityUpdate", at = @At("TAIL"))
    public void onEntityUpdate(CallbackInfo ci) {
    	new EventLivingUpdate((EntityLivingBase) (Object) this).call();
    }
    
	@Inject(method = "getArmSwingAnimationEnd", at = @At("HEAD"), cancellable = true)
	public void changeSwingSpeed(CallbackInfoReturnable<Integer> cir) {
		
		SlowSwingMod mod = SlowSwingMod.getInstance();
		
		if(mod.isToggled()) {
			cir.setReturnValue(mod.getDelaySetting().getValueInt());
		}
	}
	
    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void mouseDelayFix(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            cir.setReturnValue(super.getLook(partialTicks));
        }
    }
    
	@Override
	public int getArmSwingAnimation() {
		return getArmSwingAnimationEnd();
	}
}
