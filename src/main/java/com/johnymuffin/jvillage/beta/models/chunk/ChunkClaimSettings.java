package com.johnymuffin.jvillage.beta.models.chunk;

import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.models.Village;

import java.util.UUID;

public class ChunkClaimSettings extends VChunk{

    private Village village;
    private final long claimTime;
    private final UUID claimedBy;
    private double price;

    public ChunkClaimSettings(Village village, JsonObject jsonObject, String worldName) {
        super(worldName, jsonObject.get("x").getAsInt(), jsonObject.get("z").getAsInt());
        this.claimTime = jsonObject.get("claimTime").getAsLong();
        this.claimedBy = UUID.fromString(jsonObject.get("claimedBy").getAsString());
        this.price = jsonObject.has("price") ? jsonObject.get("price").getAsDouble() : 0.0;
    }

    public ChunkClaimSettings(Village village, long claimTime, UUID claimedBy, VChunk vChunk, double price) {
        super(vChunk.getWorldName(), vChunk.getX(), vChunk.getZ());
        this.claimTime = claimTime;
        this.claimedBy = claimedBy;
        this.price = price;
    }

    public JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("claimTime", claimTime);
        jsonObject.addProperty("claimedBy", claimedBy.toString());
        jsonObject.addProperty("x", this.getX());
        jsonObject.addProperty("z", this.getZ());
        jsonObject.addProperty("price", price);
        return jsonObject;
    }

    public long getClaimTime() {
        return claimTime;
    }

    public UUID getClaimedBy() {
        return claimedBy;
    }

    public double getPrice() {
        return price;
    }

}
