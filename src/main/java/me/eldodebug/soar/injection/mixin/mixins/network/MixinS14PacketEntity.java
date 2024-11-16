package me.eldodebug.soar.injection.mixin.mixins.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import me.eldodebug.soar.injection.interfaces.IMixinS14PacketEntity;
import net.minecraft.network.play.server.S14PacketEntity;

@Mixin(S14PacketEntity.class)
public class MixinS14PacketEntity implements IMixinS14PacketEntity {

	@Shadow
    protected int entityId;
	
	@Shadow
    protected byte posX;
	
	@Shadow
    protected byte posY;
	
	@Shadow
    protected byte posZ;

	@Override
	public int getEntityId() {
		return entityId;
	}

	@Override
	public byte getPosX() {
		return posX;
	}

	@Override
	public byte getPosY() {
		return posY;
	}

	@Override
	public byte getPosZ() {
		return posZ;
	}
}
