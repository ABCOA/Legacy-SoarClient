package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventLivingUpdate;
import me.eldodebug.soar.management.event.impl.EventLoadWorld;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.utils.LocationUtils;
import me.eldodebug.soar.utils.MathUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumChatFormatting;

public class DamageParticlesMod extends Mod {

	private HashMap<EntityLivingBase, Float> healthMap = new HashMap<>();
	private ArrayList<Particle> particles = new ArrayList<Particle>();
	private boolean canRemove;
	private Particle removeParticle;
	
	public DamageParticlesMod() {
		super(TranslateText.DAMAGE_PARTICLES, TranslateText.DAMAGE_PARTICLES_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onTick(EventTick event) {
		
		if(canRemove) {
			particles.remove(removeParticle);
		}
		
		particles.forEach(particle -> {
			particle.ticks++;

			if (particle.ticks <= 10) {
				particle.location.setY(particle.location.getY() + particle.ticks * 0.005);
			}

			if (particle.ticks > 20) {
				canRemove = true;
				removeParticle = particle;
			}
		});
	}
	
	@EventTarget
	public void onLivingUpdate(EventLivingUpdate event) {
		
		EntityLivingBase entity = event.getEntity();

		if (entity == this.mc.thePlayer) {
			return;
		}

		if (!healthMap.containsKey(entity)) {
			healthMap.put(entity, entity.getHealth());
		}

		float before = healthMap.get(entity);
		float after = entity.getHealth();

		if (before != after) {
			String text;

			if ((before - after) < 0) {
				text = EnumChatFormatting.GREEN + "" + MathUtils.roundToPlace((before - after) * -1, 1);
			} else {
				text = EnumChatFormatting.YELLOW + "" + MathUtils.roundToPlace((before - after), 1);
			}

			LocationUtils location = new LocationUtils(entity);

			location.setY(entity.getEntityBoundingBox().minY
					+ ((entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2));

			location.setX((location.getX() - 0.5) + (new Random(System.currentTimeMillis()).nextInt(5) * 0.1));
			location.setZ((location.getZ() - 0.5) + (new Random(System.currentTimeMillis() + 1).nextInt(5) * 0.1));

			particles.add(new Particle(location, text));

			healthMap.remove(entity);
			healthMap.put(entity, entity.getHealth());
		}
	}
	
	@EventTarget
	public void onRender3D(EventRender3D event) {
		
		for (Particle particle : this.particles) {
			double x = particle.location.getX() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosX();
			double y = particle.location.getY() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosY();
			double z = particle.location.getZ() - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosZ();

			GlStateManager.pushMatrix();

			GlStateManager.enablePolygonOffset();
			GlStateManager.doPolygonOffset(1.0F, -1500000.0F);

			GlStateManager.translate((float) x, (float) y, (float) z);
			GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
			GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
			double scale = 0.03;
			GlStateManager.scale(-scale, -scale, scale);

	        GL11.glDisable(2929);
	        GL11.glEnable(3042);
	        GL11.glDisable(3553);
	        GL11.glBlendFunc(770, 771);
	        GL11.glDepthMask(true);
	        GL11.glEnable(2848);
	        GL11.glHint(3154, 4354);
	        GL11.glHint(3155, 4354);
	        GL11.glEnable(3553);
	        GL11.glDisable(3042);
	        GL11.glEnable(2929);
	        GL11.glDisable(2848);
	        GL11.glHint(3154, 4352);
	        GL11.glHint(3155, 4352);
	        
			GL11.glDepthMask(false);
			fr.drawStringWithShadow(particle.text, -(mc.fontRendererObj.getStringWidth(particle.text) / 2), -(mc.fontRendererObj.FONT_HEIGHT - 1), 0);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glDepthMask(true);

			GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
			GlStateManager.disablePolygonOffset();

			GlStateManager.popMatrix();
		}
	}
	
	@EventTarget
	public void onLoadWorld(EventLoadWorld event) {
		this.particles.clear();
		this.healthMap.clear();
	}
	
	private class Particle {

		public int ticks;
		public LocationUtils location;
		public String text;
		
		public Particle(LocationUtils location, String text) {
			this.location = location;
			this.text = text;
			this.ticks = 0;
		}
	}
}
