package me.eldodebug.soar.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import me.eldodebug.soar.mobends.animation.MoBendsAnimation;
import me.eldodebug.soar.mobends.client.model.ModelRendererBends;
import me.eldodebug.soar.mobends.client.model.entity.ModelBendsPlayer;
import me.eldodebug.soar.mobends.data.Data_Player;
import me.eldodebug.soar.mobends.data.MoBends_EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;

public class Animation_Stand extends MoBendsAnimation {
	
	public String getName(){
		return "stand";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, MoBends_EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(0.0F,0.1f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(5);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-5);
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX);
		((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY);
		
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ( -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*-5  );
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(  -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*5  );
	}
}
