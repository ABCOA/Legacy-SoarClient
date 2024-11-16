package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.impl.projectiletrail.ProjectileTrailType;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.Vec3;

public class ProjectileTrailMod extends Mod {

	private ProjectileTrailType type;
	
    private ArrayList<Object> throwables = new ArrayList<>();
    private int ticks;
    
    private ComboSetting mode = new ComboSetting(TranslateText.TYPE, this, TranslateText.HEARTS, new ArrayList<Option>() {
    	private static final long serialVersionUID = 1L; {
    		for(ProjectileTrailType t : ProjectileTrailType.values()) {
    			add(new Option(t.getNameTranslate()));
    		}
    	}
    });
    
	public ProjectileTrailMod() {
		super(TranslateText.PROJECTILE_TRAIL, TranslateText.PROJECTILE_TRAIL_DESCRIPTION, ModCategory.PLAYER);
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		
		type = ProjectileTrailType.getTypeByKey(mode.getOption().getNameKey());
		ticks = ticks >= 20 ? 0 : ticks + 2;
        
        updateThrowables();
        Iterator<Entity> iterator = mc.theWorld.getLoadedEntityList().iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (entity != null && (isValidEntity(entity) || throwables.contains(entity)) && entity.getDistanceToEntity(mc.thePlayer) > 3.0F) {
                spawnParticle(type, entity.getPositionVector());
            }
        }
	}
	
    public void spawnParticle(ProjectileTrailType trail, Vec3 vector) {
        if (trail != ProjectileTrailType.GREEN_STAR && trail != ProjectileTrailType.HEARTS || ticks % 4 == 0) {
            if (trail != ProjectileTrailType.MUSIC_NOTES || ticks % 2 == 0) {
                float translate = trail.translate;
                float velocity = trail.velocity;

                for (int i = 0; i < trail.count; ++i) {
                    Random random = new Random();
                    float x = random.nextFloat() * translate * 2.0F - translate;
                    float y = random.nextFloat() * translate * 2.0F - translate;
                    float z = random.nextFloat() * translate * 2.0F - translate;
                    float xVel = random.nextFloat() * velocity * 2.0F - velocity;
                    float yVel = random.nextFloat() * velocity * 2.0F - velocity;
                    float zVel = random.nextFloat() * velocity * 2.0F - velocity;
                    double d0 = (double) x + vector.xCoord;
                    double d1 = (double) y + vector.yCoord;
                    double d2 = (double) z + vector.zCoord;

                    mc.theWorld.spawnParticle(trail.particle, true, d0, d1, d2, (double) xVel, (double) yVel, (double) zVel, new int[0]);
                }
            }
        }
    }
	
    public boolean isValidEntity(Entity entity) {
        if (entity.posX == entity.prevPosX && entity.posY == entity.prevPosY && entity.posZ == entity.prevPosZ) {
            return false;
        } else {
            if (entity instanceof EntityArrow) {
                if (((EntityArrow) entity).shootingEntity != null && ((EntityArrow) entity).shootingEntity.equals(mc.thePlayer)) {
                    return true;
                }
            } else if (entity instanceof EntityFishHook) {
                if (((EntityFishHook) entity).angler != null && ((EntityFishHook) entity).angler.equals(mc.thePlayer)) {
                    return true;
                }
            } else if (entity instanceof EntityThrowable && entity.ticksExisted == 1 && entity.getDistanceSqToEntity(mc.thePlayer) <= 11.0D && !throwables.contains(entity)) {
                throwables.add((EntityThrowable) entity);
                return true;
            }

            return false;
        }
    }
	
    public void updateThrowables() {

        Iterator<?> iterator = throwables.iterator();

        while (iterator.hasNext()) {
            EntityThrowable throwable = (EntityThrowable) iterator.next();

            if (throwable == null || throwable.isDead) {
                iterator.remove();
            }
        }
    }
}
