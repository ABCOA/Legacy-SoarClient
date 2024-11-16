package me.eldodebug.soar.injection.mixin.mixins.gui;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {
	
    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }
}
