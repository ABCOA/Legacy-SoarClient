package me.eldodebug.soar.discord.ipc.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.time.OffsetDateTime;

public class RichPresence {
	
    private final String state;
    private final String details;
    private final OffsetDateTime startTimestamp;
    private final OffsetDateTime endTimestamp;
    private final String largeImageKey;
    private final String largeImageText;
    private final String smallImageKey;
    private final String smallImageText;
    private final String partyId;
    private final int partySize;
    private final int partyMax;
    private final String matchSecret;
    private final String joinSecret;
    private final String spectateSecret;
    private final boolean instance;
    
    public RichPresence(String state, String details, OffsetDateTime startTimestamp, OffsetDateTime endTimestamp, 
            String largeImageKey, String largeImageText, String smallImageKey, String smallImageText, 
            String partyId, int partySize, int partyMax, String matchSecret, String joinSecret, 
            String spectateSecret, boolean instance) {
        this.state = state;
        this.details = details;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.largeImageKey = largeImageKey;
        this.largeImageText = largeImageText;
        this.smallImageKey = smallImageKey;
        this.smallImageText = smallImageText;
        this.partyId = partyId;
        this.partySize = partySize;
        this.partyMax = partyMax;
        this.matchSecret = matchSecret;
        this.joinSecret = joinSecret;
        this.spectateSecret = spectateSecret;
        this.instance = instance;
    }

    public JsonObject toJson() {
    	
        JsonObject timestampsObject = new JsonObject();

        if (startTimestamp != null) {
            timestampsObject.addProperty("start", startTimestamp.toEpochSecond());
        }

        if (endTimestamp != null) {
            timestampsObject.addProperty("end", endTimestamp.toEpochSecond());
        }

        JsonObject assetsObject = new JsonObject();

        if (largeImageKey != null) {
            assetsObject.addProperty("large_image", largeImageKey);
        }

        if (largeImageText != null) {
            assetsObject.addProperty("large_text", largeImageText);
        }

        if (smallImageKey != null) {
            assetsObject.addProperty("small_image", smallImageKey);
        }

        if (smallImageText != null) {
            assetsObject.addProperty("small_text", smallImageText);
        }

        JsonObject partyObject = null;
        
        if (partyId != null) {
            partyObject = new JsonObject();
            partyObject.addProperty("id", partyId);
            JsonArray partySizeArray = new JsonArray();
            partySizeArray.add(new JsonPrimitive(partySize));
            partySizeArray.add(new JsonPrimitive(partyMax));
            partyObject.add("size", partySizeArray);
        }

        JsonObject secretsObject = new JsonObject();

        if (joinSecret != null) {
            secretsObject.addProperty("join", joinSecret);
        }

        if (spectateSecret != null) {
            secretsObject.addProperty("spectate", spectateSecret);
        }

        if (matchSecret != null) {
            secretsObject.addProperty("match", matchSecret);
        }

        JsonObject jsonObject = new JsonObject();

        if (state != null) {
            jsonObject.addProperty("state", state);
        }

        if (details != null) {
            jsonObject.addProperty("details", details);
        }

        jsonObject.add("timestamps", timestampsObject);
        jsonObject.add("assets", assetsObject);

        if (partyObject != null)
            jsonObject.add("party", partyObject);

        jsonObject.add("secrets", secretsObject);
        jsonObject.addProperty("instance", instance);

        return jsonObject;
    }

    public static class Builder {
    	
        private String state;
        private String details;
        private OffsetDateTime startTimestamp;
        private OffsetDateTime endTimestamp;
        private String largeImageKey;
        private String largeImageText;
        private String smallImageKey;
        private String smallImageText;
        private String partyId;
        private int partySize;
        private int partyMax;
        private String matchSecret;
        private String joinSecret;
        private String spectateSecret;
        private boolean instance;

        public RichPresence build() {
            return new RichPresence(state, details, startTimestamp, endTimestamp, 
                    largeImageKey, largeImageText, smallImageKey, smallImageText, 
                    partyId, partySize, partyMax, matchSecret, joinSecret, 
                    spectateSecret, instance);
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setStartTimestamp(OffsetDateTime startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        public Builder setEndTimestamp(OffsetDateTime endTimestamp) {
            this.endTimestamp = endTimestamp;
            return this;
        }

        public Builder setLargeImage(String largeImageKey, String largeImageText) {
            this.largeImageKey = largeImageKey;
            this.largeImageText = largeImageText;
            return this;
        }

        public Builder setLargeImage(String largeImageKey) {
            return setLargeImage(largeImageKey, null);
        }

        public Builder setSmallImage(String smallImageKey, String smallImageText) {
            this.smallImageKey = smallImageKey;
            this.smallImageText = smallImageText;
            return this;
        }

        public Builder setSmallImage(String smallImageKey) {
            return setSmallImage(smallImageKey, null);
        }

        public Builder setParty(String partyId, int partySize, int partyMax) {
            this.partyId = partyId;
            this.partySize = partySize;
            this.partyMax = partyMax;
            return this;
        }

        public Builder setMatchSecret(String matchSecret) {
            this.matchSecret = matchSecret;
            return this;
        }

        public Builder setJoinSecret(String joinSecret) {
            this.joinSecret = joinSecret;
            return this;
        }

        public Builder setSpectateSecret(String spectateSecret) {
            this.spectateSecret = spectateSecret;
            return this;
        }

        public Builder setInstance(boolean instance) {
            this.instance = instance;
            return this;
        }
    }
}
