package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventAttackEntity;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class BloodParticlesMod extends Mod {

	private EntityLivingBase target;
	
	private NumberSetting amountSetting = new NumberSetting(TranslateText.AMOUNT, this, 2, 1, 10, true);
	private BooleanSetting soundSetting = new BooleanSetting(TranslateText.SOUND, this, true);
	
	public BloodParticlesMod() {
		super(TranslateText.BLOOD_PARTICLES, TranslateText.BLOOD_PARTICLES_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onAttackEntity(EventAttackEntity event) {
		
		if(!(event.getEntity() instanceof EntityLivingBase)) {
			return;
		}
		
    	if(target != null) {
    		 for (int i = 0; i < amountSetting.getValueInt(); i++) {
                mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + target.height - 0.75, target.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
    		 }
  		 }
  	
		 if(soundSetting.isToggled() && target != null) {
 			 mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 4.0F, 1.2F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
 		 }
	}
	
	@EventTarget
	public void onUpdate(EventUpdate event) {
		if(mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
			if(mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
				target = (EntityLivingBase) mc.objectMouseOver.entityHit;
			}
		}
	}
}
