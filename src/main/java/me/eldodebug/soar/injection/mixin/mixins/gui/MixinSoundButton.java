package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.injection.mixin.access.ISoundButtonAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.GuiScreenOptionsSounds$Button")
public abstract class MixinSoundButton implements ISoundButtonAccessor {

    @Shadow private float field_146156_o;

    @Override
    public float soar$getSoundProgress() {
        return field_146156_o;
    }
}
