package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.entity.Entity;

public class EventAttackEntity extends Event {

	private Entity entity;
	
	public EventAttackEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}
}
