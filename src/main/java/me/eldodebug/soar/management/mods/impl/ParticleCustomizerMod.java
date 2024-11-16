package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventAttackEntity;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;

public class ParticleCustomizerMod extends Mod {

	private BooleanSetting alwaysSharpnessSetting = new BooleanSetting(TranslateText.ALWAYS_SHARPNESS, this, false);
	private BooleanSetting alwaysCriticalsSetting = new BooleanSetting(TranslateText.ALWAYS_CRITICALS, this, false);
	private BooleanSetting sharpnessSetting = new BooleanSetting(TranslateText.SHARPNESS, this, true);
	private BooleanSetting criticalsSetting = new BooleanSetting(TranslateText.CRITICALS, this, false);
	
	private NumberSetting sharpnessAmountSetting = new NumberSetting(TranslateText.SHARPNESS_AMOUNT, this, 2, 1, 10, true);
	private NumberSetting criticalsAmountSetting = new NumberSetting(TranslateText.CRITICALS_AMOUNT, this, 2, 1, 10, true);
	
	public ParticleCustomizerMod() {
		super(TranslateText.PARTICLE_CUSTOMIZER, TranslateText.PARTICLE_CUSTOMIZER_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onAttackEntity(EventAttackEntity event) {
		
		EntityPlayer player = mc.thePlayer;
		
		int sMultiplier = sharpnessAmountSetting.getValueInt();
		int cMultiplier = criticalsAmountSetting.getValueInt();
		
		if(!(event.getEntity() instanceof EntityLivingBase)) {
			return;
		}
		
		boolean critical = criticalsSetting.isToggled() && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Potion.blindness) && player.ridingEntity == null;
		boolean alwaysSharpness = alwaysSharpnessSetting.isToggled();
		boolean sharpness = sharpnessSetting.isToggled() && EnchantmentHelper.getModifierForCreature(player.getHeldItem(), ((EntityLivingBase) event.getEntity()).getCreatureAttribute()) > 0;
		boolean alwaysCriticals = alwaysCriticalsSetting.isToggled();
		
		if(critical || alwaysCriticals) {
			for(int i = 0; i < cMultiplier - 1; i++) {
				mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT);
			}
		}
		
		if(alwaysSharpness || sharpness) {
			for(int i = 0; i < sMultiplier - 1; i++) {
				mc.effectRenderer.emitParticleAtEntity(event.getEntity(), EnumParticleTypes.CRIT_MAGIC);
			}
		}
	}
}
