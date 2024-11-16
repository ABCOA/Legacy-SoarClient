package me.eldodebug.soar.injection.mixin.mixins.multiplayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.management.mods.impl.OldAnimationsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Final
    @Shadow
    private final Minecraft mc = Minecraft.getMinecraft();
    
    @Mutable
    @Final
    @Shadow
    private final NetHandlerPlayClient netClientHandler;
    
    @Shadow
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);
    
    @Shadow
    private boolean isHittingBlock;
    
    @Shadow
    private float curBlockDamageMP;
    
    protected MixinPlayerControllerMP(NetHandlerPlayClient netClientHandler) {
        this.netClientHandler = netClientHandler;
    }

    @Overwrite
    public void resetBlockRemoving() {
    	
        if (isHittingBlock) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
        }
        
        isHittingBlock = false;
        curBlockDamageMP = 0.0F;
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, -1);
    }

    @Inject(method = "getIsHittingBlock", at = @At("HEAD"), cancellable = true)
    private void cancelHit(CallbackInfoReturnable<Boolean> cir) {
    	
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
    	
    	if(mod.isToggled() && mod.getPushingSetting().isToggled() && mod.getBlockHitSetting().isToggled()) {
    		cir.setReturnValue(false);
    	}
    }
}
