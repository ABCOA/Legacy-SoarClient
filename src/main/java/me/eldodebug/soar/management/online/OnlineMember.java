package me.eldodebug.soar.management.online;

import java.util.UUID;

public final class OnlineMember {
    private final UUID uuid;
    private final String username;
    private final CapeDescriptor cape;
    public OnlineMember(UUID uuid, String username, CapeDescriptor cape) {
        this.uuid = uuid; this.username = username == null ? "" : username; this.cape = cape;
    }
    public UUID getUuid() { return uuid; }
    public UUID getUUID() { return uuid; }
    public String getUsername() { return username; }
    public CapeDescriptor getCape() { return cape; }
}
