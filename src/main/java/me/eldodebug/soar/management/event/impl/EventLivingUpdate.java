package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class EventLivingUpdate extends Event {
	
	private EntityLivingBase entity;
	
	public EventLivingUpdate(EntityLivingBase entity) {
		this.entity = entity;
	}

	public EntityLivingBase getEntity() {
		return entity;
	}
}