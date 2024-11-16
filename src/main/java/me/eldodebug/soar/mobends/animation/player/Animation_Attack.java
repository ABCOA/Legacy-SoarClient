package me.eldodebug.soar.mobends.animation.player;

import me.eldodebug.soar.mobends.animation.MoBendsAnimation;
import me.eldodebug.soar.mobends.client.model.entity.ModelBendsPlayer;
import me.eldodebug.soar.mobends.data.Data_Player;
import me.eldodebug.soar.mobends.data.MoBends_EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class Animation_Attack extends MoBendsAnimation{
	public String getName(){
		return "attack";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, MoBends_EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		if(player.getCurrentEquippedItem() != null){
				if(data.ticksAfterPunch < 10){
					if(data.currentAttack == 1){
						Animation_Attack_Combo0.animate((EntityPlayer) argEntity,model,data);
					}else if(data.currentAttack == 2){
						Animation_Attack_Combo1.animate((EntityPlayer) argEntity,model,data);
					}else if(data.currentAttack == 3){
						Animation_Attack_Combo2.animate((EntityPlayer) argEntity,model,data);
					}
				}else if(data.ticksAfterPunch < 60){
					Animation_Attack_Stance.animate((EntityPlayer) argEntity,model,data);
				}
		}else{
			if(data.ticksAfterPunch < 10){
				Animation_Attack_Punch.animate((EntityPlayer) argEntity,model,data);
			}else if(data.ticksAfterPunch < 60){
				Animation_Attack_PunchStance.animate((EntityPlayer) argEntity,model,data);
			}
		}
	}
}
