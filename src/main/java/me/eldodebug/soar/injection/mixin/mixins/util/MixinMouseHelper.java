package me.eldodebug.soar.injection.mixin.mixins.util;

import me.eldodebug.soar.management.mods.impl.RawInputMod;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

	@Shadow
    public int deltaX;
    
    @Shadow
    public int deltaY;
    
    @Inject(method = "mouseXYChange", at = @At("HEAD"), cancellable = true)
    public void onRawInput(CallbackInfo ci) {
    	
    	RawInputMod mod = RawInputMod.getInstance();
    	
    	if(mod.isToggled() && Mouse.isGrabbed() && mod.isAvailable()) {
        	ci.cancel();
        	deltaX = (int) mod.getDx();
        	deltaY = (int) -mod.getDy();
        	mod.getThread().reset();
    	}
    }
}
