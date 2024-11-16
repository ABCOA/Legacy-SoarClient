package me.eldodebug.soar.discord.ipc.entities;

public enum DiscordBuild {
    CANARY("//canary.discordapp.com/api"),
    PTB("//ptb.discordapp.com/api"),
    STABLE("//discordapp.com/api"),
    ANY;

    private final String endpoint;

    DiscordBuild(String endpoint) {
        this.endpoint = endpoint;
    }

    DiscordBuild() {
        this(null);
    }

    public static DiscordBuild from(String endpoint) {
        for(DiscordBuild value : values()) {
            if(value.endpoint != null && value.endpoint.equals(endpoint)) {
                return value;
            }
        }
        return ANY;
    }
}
