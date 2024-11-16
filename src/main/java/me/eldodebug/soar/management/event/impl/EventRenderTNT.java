package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.item.EntityTNTPrimed;

public class EventRenderTNT extends Event {

	private RenderTNTPrimed tntRenderer;
	private EntityTNTPrimed entity;
	private double x, y, z;
	private float entityYaw, partialTicks;
	
	public EventRenderTNT(RenderTNTPrimed tntRenderer, EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.tntRenderer = tntRenderer;
		this.entity = entity;
		this.x = x;
		this.y = y;
		this.z = z;
		this.entityYaw = entityYaw;
		this.partialTicks = partialTicks;
	}

	public RenderTNTPrimed getTntRenderer() {
		return tntRenderer;
	}

	public EntityTNTPrimed getEntity() {
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

	public float getEntityYaw() {
		return entityYaw;
	}

	public float getPartialTicks() {
		return partialTicks;
	}
}