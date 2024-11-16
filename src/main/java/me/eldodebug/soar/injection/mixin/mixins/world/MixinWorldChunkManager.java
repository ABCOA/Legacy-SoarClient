package me.eldodebug.soar.injection.mixin.mixins.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.WeatherChangerMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.world.biome.WorldChunkManager;

@Mixin(WorldChunkManager.class)
public class MixinWorldChunkManager {

	@Inject(method = "getTemperatureAtHeight", at = @At("HEAD"), cancellable = true)
	public void preGetTemperatureAtHeight(float p_76939_1_, int p_76939_2_, CallbackInfoReturnable<Float> cir) {
		
		WeatherChangerMod mod = WeatherChangerMod.getInstance();
		ComboSetting setting = mod.getWeatherSetting();
		Option weather = setting.getOption();
		
		if(mod.isToggled() && weather.getTranslate().equals(TranslateText.SNOW)) {
			cir.setReturnValue(0F);
		}
	}
}
