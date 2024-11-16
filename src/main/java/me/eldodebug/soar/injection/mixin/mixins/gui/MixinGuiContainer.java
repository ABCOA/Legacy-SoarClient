package me.eldodebug.soar.injection.mixin.mixins.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.InventoryMod;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.ui.particle.ParticleEngine;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);
    
    @Shadow 
    private int dragSplittingButton;
    
    @Shadow 
    private int dragSplittingRemnant;

    private SimpleAnimation xAnimation;
    private SimpleAnimation yAnimation;
    
	private Animation xAnimationBackIn;
	private Animation yAnimationBackIn;
	
	private ParticleEngine particle = new ParticleEngine();
	
    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo ci) {
    	
    	InventoryMod mod = InventoryMod.getInstance();
    	
    	if(mod.isToggled() && mod.getAnimationSetting().isToggled()) {
    		
    		Option option = mod.getAnimationTypeSetting().getOption();
    		
    		if(option.getTranslate().equals(TranslateText.NORMAL)) {
    			xAnimation = new SimpleAnimation(0.0F);
    			yAnimation = new SimpleAnimation(0.0F);
    		}else {
    			xAnimationBackIn = new EaseBackIn(380, this.width, 2);
    			yAnimationBackIn = new EaseBackIn(380, this.height, 2);
    		}
    	}
    }
    
    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
    	
    	InventoryMod mod = InventoryMod.getInstance();
    	
    	if(!mod.isToggled() || (mod.isToggled() && mod.getBackgroundSetting().isToggled())) {
    		this.drawDefaultBackground();
    	}
    	
    	if(mod.isToggled() && mod.getParticleSetting().isToggled()) {
    		particle.draw(mouseX, mouseY);
    	}
    	
    	if(mod.isToggled() && mod.getAnimationSetting().isToggled()) {
    		
			double xmod = 0;
			double ymod = 0;
			
    		Option option = mod.getAnimationTypeSetting().getOption();
    		
			if(option.getTranslate().equals(TranslateText.NORMAL)) {
				
				xAnimation.setAnimation(this.width, 18);
				yAnimation.setAnimation(this.height, 18);
				
				xmod = this.width / 2 - (xAnimation.getValue() / 2);
				ymod = this.height / 2 - (yAnimation.getValue() / 2);
				
	        	GlStateManager.translate(xmod, ymod, 0);
	        	GlStateManager.scale(xAnimation.getValue() / this.width, yAnimation.getValue() / this.height, 1);	
			}else {
				xmod = this.width / 2 - (xAnimationBackIn.getValue() / 2);
				ymod = this.height / 2 - (yAnimationBackIn.getValue() / 2);
				
	        	GlStateManager.translate(xmod, ymod, 0);
	        	GlStateManager.scale(xAnimationBackIn.getValue() / this.width, yAnimationBackIn.getValue() / this.height, 1);	
			}
    	}
    }
    
    @Inject(method = "updateDragSplitting", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void fixRemnants(CallbackInfo ci) {
        if (this.dragSplittingButton == 2) {
            this.dragSplittingRemnant = mc.thePlayer.inventory.getItemStack().getMaxStackSize();
            ci.cancel();
        }
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton - 100 == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.thePlayer.closeScreen();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void postMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        checkHotbarKeys(mouseButton - 100);
    }
    
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawDefaultBackground()V"))
    public void removeDrawDefaultBackground() {}
}
