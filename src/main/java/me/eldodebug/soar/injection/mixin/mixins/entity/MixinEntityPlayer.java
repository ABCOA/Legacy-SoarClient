package me.eldodebug.soar.injection.mixin.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.IMixinEntityPlayer;
import me.eldodebug.soar.management.event.impl.EventAttackEntity;
import me.eldodebug.soar.management.event.impl.EventJump;
import me.eldodebug.soar.management.mods.impl.skin3d.render.CustomizableModelPart;
import me.eldodebug.soar.management.mods.impl.waveycapes.sim.StickSimulation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IMixinEntityPlayer {

    private CustomizableModelPart headLayer;
	private CustomizableModelPart[] skinLayer;
	
    private StickSimulation stickSimulation = new StickSimulation();
    
	@Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
	public void attackEntity(Entity entity, CallbackInfo ci) {
		if(entity.canAttackWithItem()) {
			new EventAttackEntity(entity).call();
		}
	}
	
	@Inject(method = "jump", at = @At("HEAD"))
    public void preJump(CallbackInfo ci) {
		new EventJump().call();
	}
	
    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void moveCloakUpdate(CallbackInfo info) {
        if((Object)this instanceof EntityPlayer) {
            simulate((EntityPlayer)(Object)this);
        }
    }
    
    @Override
    public StickSimulation getSimulation() {
        return stickSimulation;
    }
    
	@Override
	public CustomizableModelPart[] getSkinLayers() {
		return skinLayer;
	}
	
	@Override
	public void setupSkinLayers(CustomizableModelPart[] box) {
		this.skinLayer = box;
	}
	
	@Override
	public CustomizableModelPart getHeadLayers() {
		return headLayer;
	}
	
	@Override
	public void setupHeadLayers(CustomizableModelPart box) {
		this.headLayer = box;
	}
}
