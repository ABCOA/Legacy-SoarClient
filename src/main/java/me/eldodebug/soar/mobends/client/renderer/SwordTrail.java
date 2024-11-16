package me.eldodebug.soar.mobends.client.renderer;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import me.eldodebug.soar.management.mods.impl.MoBendsMod;
import me.eldodebug.soar.mobends.client.model.ModelRendererBends;
import me.eldodebug.soar.mobends.client.model.entity.ModelBendsPlayer;
import me.eldodebug.soar.mobends.util.MoBendsGUtil;
import me.eldodebug.soar.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class SwordTrail {
	
	public List<TrailPart> trailPartList = new ArrayList<TrailPart>();
	
	public SwordTrail(){
		
	}
	
	public void reset(){
		this.trailPartList.clear();
	}
	
	public class TrailPart{
		
		public ModelRendererBends body, arm, foreArm;
		
		public Vector3f renderRotation = new Vector3f();
		public Vector3f renderOffset = new Vector3f();
		public Vector3f itemRotation = new Vector3f();
		
		float ticksExisted;
		
		public TrailPart(ModelBendsPlayer argModel){
			body = new ModelRendererBends(argModel);
			arm = new ModelRendererBends(argModel);
			foreArm = new ModelRendererBends(argModel);
		}
	}
	
	public void render(ModelBendsPlayer model){
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("soar/white.png"));
		
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
    	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		GL11.glBegin(GL11.GL_QUADS);
		
		for(int i = 0;i < this.trailPartList.size();i++){
			TrailPart part = this.trailPartList.get(i);
			
			float alpha = part.ticksExisted/5.0f;
			alpha = MoBendsGUtil.max(alpha, 1.0f);
			alpha = 1.0f-alpha;
			
			if(MoBendsMod.getInstance().getCustomColorSetting().isToggled()) {
				ColorUtils.setColor(MoBendsMod.getInstance().getColorSetting().getColor().getRGB(), alpha);
			}else {
				GL11.glColor4f(1, 1, 1,alpha);
			}
			
			Vector3f[] point = new Vector3f[]{
				new Vector3f(0,0,-8+8*alpha),
				new Vector3f(0,0,-8-8*alpha),
			};
			
			MoBendsGUtil.rotateX(point, part.itemRotation.getX());
			MoBendsGUtil.rotateY(point, part.itemRotation.getY());
			MoBendsGUtil.rotateZ(point, part.itemRotation.getZ());
			
			MoBendsGUtil.translate(point, new Vector3f(-1,-6,0));
			MoBendsGUtil.rotateX(point, part.foreArm.rotateAngleX/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateY(point, part.foreArm.rotateAngleY/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateZ(point, part.foreArm.rotateAngleZ/(float)Math.PI*180.0f);
			
			MoBendsGUtil.rotateX(point, ((ModelRendererBends)part.foreArm).pre_rotation.getX());
			MoBendsGUtil.rotateY(point, ((ModelRendererBends)part.foreArm).pre_rotation.getY());
			MoBendsGUtil.rotateZ(point, -((ModelRendererBends)part.foreArm).pre_rotation.getZ());
			
			MoBendsGUtil.translate(point, new Vector3f(0,-6+2,0));
			MoBendsGUtil.rotateX(point, part.arm.rotateAngleX/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateY(point, part.arm.rotateAngleY/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateZ(point, part.arm.rotateAngleZ/(float)Math.PI*180.0f);
			
			MoBendsGUtil.rotateX(point, ((ModelRendererBends)part.arm).pre_rotation.getX());
			MoBendsGUtil.rotateY(point, ((ModelRendererBends)part.arm).pre_rotation.getY());
			MoBendsGUtil.rotateZ(point, -((ModelRendererBends)part.arm).pre_rotation.getZ());
			
			MoBendsGUtil.translate(point, new Vector3f(-5,10,0));
			MoBendsGUtil.rotateX(point, part.body.rotateAngleX/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateY(point, part.body.rotateAngleY/(float)Math.PI*180.0f);
			MoBendsGUtil.rotateZ(point, part.body.rotateAngleZ/(float)Math.PI*180.0f);
			
			MoBendsGUtil.rotateX(point, ((ModelRendererBends)part.body).pre_rotation.getX());
			MoBendsGUtil.rotateY(point, ((ModelRendererBends)part.body).pre_rotation.getY());
			MoBendsGUtil.rotateZ(point, ((ModelRendererBends)part.body).pre_rotation.getZ());
			MoBendsGUtil.translate(point, new Vector3f(0,12,0));
			
			MoBendsGUtil.rotateX(point, part.renderRotation.getX());
			MoBendsGUtil.rotateY(point, part.renderRotation.getY());
			MoBendsGUtil.translate(point, part.renderOffset);
			
			if(i > 0){
				GL11.glVertex3f(point[1].x,point[1].y,point[1].z);
				GL11.glVertex3f(point[0].x,point[0].y,point[0].z);
				
				GL11.glVertex3f(point[0].x,point[0].y,point[0].z);
				GL11.glVertex3f(point[1].x,point[1].y,point[1].z);
			}else{
				GL11.glVertex3f(point[0].x,point[0].y,point[0].z);
				GL11.glVertex3f(point[1].x,point[1].y,point[1].z);
			}
			
			if(i == this.trailPartList.size()-1){
				GL11.glVertex3f(point[1].x,point[1].y,point[1].z);
				GL11.glVertex3f(point[0].x,point[0].y,point[0].z);
			}
		}
		
		GL11.glEnd();
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
	}
	
	public void add(ModelBendsPlayer argModel){
		
		TrailPart newPart = new TrailPart(argModel);
		
		newPart.body.sync((ModelRendererBends)argModel.bipedBody);
		newPart.body.setPosition(argModel.bipedBody.rotationPointX,argModel.bipedBody.rotationPointY,argModel.bipedBody.rotationPointZ);
		newPart.body.setOffset(argModel.bipedBody.offsetX,argModel.bipedBody.offsetY,argModel.bipedBody.offsetZ);
		newPart.arm.sync((ModelRendererBends)argModel.bipedRightArm);
		newPart.arm.setPosition(argModel.bipedRightArm.rotationPointX,argModel.bipedRightArm.rotationPointY,argModel.bipedRightArm.rotationPointZ);
		newPart.arm.setOffset(argModel.bipedRightArm.offsetX,argModel.bipedRightArm.offsetY,argModel.bipedRightArm.offsetZ);
		newPart.foreArm.sync((ModelRendererBends)argModel.bipedRightForeArm);
		newPart.foreArm.setPosition(argModel.bipedRightForeArm.rotationPointX,argModel.bipedRightForeArm.rotationPointY,argModel.bipedRightForeArm.rotationPointZ);
		newPart.foreArm.setOffset(argModel.bipedRightForeArm.offsetX,argModel.bipedRightForeArm.offsetY,argModel.bipedRightForeArm.offsetZ);
			
		newPart.renderOffset.set(argModel.renderOffset.vSmooth);
		newPart.renderRotation.set(argModel.renderRotation.vSmooth);
		newPart.itemRotation.set(argModel.renderItemRotation.vSmooth);
		
		this.trailPartList.add(newPart);
	}
	
	public void update(float argPartialTicks){
		
		for(int i = 0;i < this.trailPartList.size();i++){
			this.trailPartList.get(i).ticksExisted+=argPartialTicks;
		}
		
		for(int i = 0;i < this.trailPartList.size();i++){
			if(this.trailPartList.get(i).ticksExisted > 20){
				this.trailPartList.remove(this.trailPartList.get(i));
			}
		}
	}
}
