package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class EventRendererLivingEntity extends Event {
	
	private RendererLivingEntity<EntityLivingBase> renderer;
	private Entity entity;
	private double x, y, z;
	
	public EventRendererLivingEntity(RendererLivingEntity<EntityLivingBase> renderer, Entity entity, double x, double y, double z) {
		this.renderer = renderer;
		this.entity = entity;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public RendererLivingEntity<EntityLivingBase> getRenderer() {
		return renderer;
	}

	public Entity getEntity() {
		return entity;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
}