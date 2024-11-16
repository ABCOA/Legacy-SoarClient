package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventLoadWorld;
import me.eldodebug.soar.management.event.impl.EventMotionUpdate;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class KillEffectsMod extends Mod {

	private EntityLivingBase target;
    private int entityID;
    
    private BooleanSetting soundSetting = new BooleanSetting(TranslateText.SOUND, this, true);
    private ComboSetting effectSetting = new ComboSetting(TranslateText.EFFECT, this, TranslateText.BLOOD, new ArrayList<Option>(Arrays.asList(
    		new Option(TranslateText.LIGHTING), new Option(TranslateText.FLAMES), new Option(TranslateText.CLOUD), new Option(TranslateText.BLOOD))));
    
    private NumberSetting multiplierSetting = new NumberSetting(TranslateText.MULTIPLIER, this, 1, 1, 10, true);
    
	public KillEffectsMod() {
		super(TranslateText.KILL_EFFECTS, TranslateText.KILL_EFFECTS_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		if(mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
			if(mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
				target = (EntityLivingBase) mc.objectMouseOver.entityHit;
			}
		}
	}
	
	@EventTarget
	public void onPreMotionUpdate(EventMotionUpdate event) {
		
		if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100) {
			
			if (mc.thePlayer.ticksExisted > 3) {
				
				Option option = effectSetting.getOption();
				
				if(option.getTranslate().equals(TranslateText.LIGHTING)) {
					
                    EntityLightningBolt entityLightningBolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                    mc.theWorld.addEntityToWorld(entityID--, entityLightningBolt);

                    if (soundSetting.isToggled()) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("ambient.weather.thunder"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
				} else if(option.getTranslate().equals(TranslateText.FLAMES)) {
					
                    for (int i = 0; i < multiplierSetting.getValueInt(); i++) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
                    }
                    
                    if (soundSetting.isToggled()) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("item.fireCharge.use"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
				} else if(option.getTranslate().equals(TranslateText.CLOUD)) {
					
                    for (int i = 0; i < multiplierSetting.getValueInt(); i++) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CLOUD);
                    }
                    
                    if (soundSetting.isToggled()) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("fireworks.twinkle"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
				} else if(option.getTranslate().equals(TranslateText.BLOOD)) {
					
                    for (int i = 0; i < 50; i++) {
                        mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + target.height - 0.75, target.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
                    }

                    if (soundSetting.isToggled()) {
                        mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 4.0F, 1.2F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
                    }
				}
			}
			target = null;
		}
	}
	
	@EventTarget
	public void onLoadWorld(EventLoadWorld event) {
		entityID = 0;
	}
}
