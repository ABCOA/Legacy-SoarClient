package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import java.util.UUID;

public class EventLocationCape extends Event {
	
	private NetworkPlayerInfo playerInfo;
	private ResourceLocation cape;
	private UUID playerId;

	public EventLocationCape(NetworkPlayerInfo playerInfo) {
		this.playerInfo = playerInfo;
		this.playerId = playerInfo == null ? null : playerInfo.getGameProfile().getId();
	}

	public EventLocationCape(AbstractClientPlayer player, NetworkPlayerInfo playerInfo) {
		this(playerInfo);
		this.playerId = player.getUniqueID();
	}

	public UUID getPlayerId() { return playerId; }
	
	public NetworkPlayerInfo getPlayerInfo() {
		return playerInfo;
	}

	public ResourceLocation getCape() {
		return cape;
	}

	public void setCape(ResourceLocation cape) {
		this.cape = cape;
	}
}
