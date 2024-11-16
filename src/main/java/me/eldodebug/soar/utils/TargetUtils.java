package me.eldodebug.soar.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;

public class TargetUtils {

	private static AbstractClientPlayer target;
	private static Minecraft mc = Minecraft.getMinecraft();
	private static TimerUtils timer = new TimerUtils();
	
	public static void onUpdate() {
		
	    if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit != target) {
	        if (mc.objectMouseOver.entityHit instanceof AbstractClientPlayer && ServerUtils.isInTablist(mc.objectMouseOver.entityHit)) {
	            target = (AbstractClientPlayer) mc.objectMouseOver.entityHit;
	            timer.reset();
	        }
	    } else if (timer.delay(2500) && mc.objectMouseOver == null) {
	        target = null;
	        timer.reset();
	    }

	    if (target != null) {

	        if (target.isDead || mc.thePlayer.isDead) {
	            target = null;
	        } else if (mc.thePlayer != null) {
	            if (target.isInvisible() || target.getDistanceToEntity(mc.thePlayer) > 12) {
	                target = null;
	            }
	        }
	    }
	}

	public static AbstractClientPlayer getTarget() {
		return target;
	}
}