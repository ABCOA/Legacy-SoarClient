package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.injection.mixin.access.IOptionSliderAccessor;
import net.minecraft.client.gui.GuiOptionSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiOptionSlider.class)
public abstract class MixinGuiOptionSlider implements IOptionSliderAccessor {

    @Shadow private float sliderValue;

    @Override
    public float soar$getSliderValue() {
        return sliderValue;
    }
}