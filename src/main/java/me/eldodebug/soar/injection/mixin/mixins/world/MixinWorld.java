package me.eldodebug.soar.injection.mixin.mixins.world;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.eldodebug.soar.injection.interfaces.IMixinWorld;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.WeatherChangerMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.utils.EnumFacings;
import me.eldodebug.soar.viaversion.fixes.FixedSoundEngine;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

	@Shadow 
	@Final 
	public boolean isRemote;
	
    @Unique
    private int updateRange;
    
	@Shadow
    public abstract boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);
	
	@Shadow
	protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
	
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    public void preGetRainStrength(float delta, CallbackInfoReturnable<Float> cir) {
    	
    	WeatherChangerMod mod = WeatherChangerMod.getInstance();
    	ComboSetting setting = mod.getWeatherSetting();
    	Option weather = setting.getOption();
    	
        if (mod.isToggled() && weather.getTranslate().equals(TranslateText.CLEAR)) {
            cir.setReturnValue(0f);
        } else if (mod.isToggled()) {
            cir.setReturnValue(mod.getRainStrength().getValueFloat());
        }
    }

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    public void preGgetThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {
    	
    	WeatherChangerMod mod = WeatherChangerMod.getInstance();
    	ComboSetting setting = mod.getWeatherSetting();
    	Option weather = setting.getOption();
    	
        if (mod.isToggled() && !weather.getTranslate().equals(TranslateText.STORM)) {
            cir.setReturnValue(0f);
        } else if (mod.isToggled()) {
            cir.setReturnValue(mod.getThunderStrength().getValueFloat());
        }
    }
    
	@Override
	public boolean isLoaded(int x, int z, boolean allowEmpty) {
		return isChunkLoaded(x, z, allowEmpty);
	}
	
    @ModifyVariable(method = "updateEntityWithOptionalForce", at = @At("STORE"), ordinal = 1)
    private boolean checkIfWorldIsRemoteBeforeForceUpdating(boolean isForced) {
        return isForced && !this.isRemote;
    }
    
    @Inject(method = "getCollidingBoundingBoxes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filterEntities(Entity entityIn, AxisAlignedBB bb, CallbackInfoReturnable<List<AxisAlignedBB>> cir, List<AxisAlignedBB> list) {
        if (entityIn instanceof EntityTNTPrimed || entityIn instanceof EntityFallingBlock || entityIn instanceof EntityItem || entityIn instanceof EntityFX) {
            cir.setReturnValue(list);
        }
    }
    
    @Redirect(method = "getHorizon", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getHorizon()D", remap = false))
    private double alwaysZero(WorldProvider worldProvider) {
        return 0.0D;
    }
    
    @ModifyArg(method = "checkLightFor", at = @At(value="INVOKE", target="Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/BlockPos;IZ)Z", ordinal=0), index=1)
    public int reduceAreaLoadedCheckRange(int radius) {
        return 16;
    }

    @Inject(method = "checkLightFor", at = @At(value="INVOKE", target="Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal=0))
    public void calculateUpdateRange(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.updateRange = this.isAreaLoaded(pos, 18, false) ? 17 : 15;
    }

    @ModifyConstant(method = "checkLightFor", constant = @Constant(intValue=17), slice = @Slice(from=@At(value="INVOKE", target="Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal=0)), allow=2)
    public int replaceRangeConstants(int constant) {
        return this.updateRange;
    }
    
    @Redirect(method = "getRawLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] getRawLight$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] checkLightFor$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "isBlockIndirectlyGettingPowered", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] isBlockIndirectlyGettingPowered$getCachedArray() {
        return EnumFacings.FACINGS;
    }
    
    @Overwrite
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
    	return FixedSoundEngine.destroyBlock((World) (Object) this, pos, dropBlock);
    }
}
