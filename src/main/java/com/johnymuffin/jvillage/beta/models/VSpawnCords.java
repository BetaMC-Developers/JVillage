package com.johnymuffin.jvillage.beta.models;

import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.JVUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class VSpawnCords extends VCords {

    private int yaw;

    public VSpawnCords(int x, int y, int z, int yaw, String worldName) {
        super(x, y, z, worldName);
        this.yaw = yaw;
    }

    public VSpawnCords(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), JVUtility.closestYaw(location.getYaw()), location.getWorld().getName());
    }

    public VSpawnCords(JsonObject jsonObject) {
        super(jsonObject.get("x").getAsInt(),
                jsonObject.get("y").getAsInt(),
                jsonObject.get("z").getAsInt(),
                jsonObject.get("world").getAsString());
        if (jsonObject.has("yaw")) {
            yaw = jsonObject.get("yaw").getAsInt();
        } else {
            yaw = 0;
        }
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", super.getX());
        jsonObject.addProperty("y", super.getY());
        jsonObject.addProperty("z", super.getZ());
        jsonObject.addProperty("yaw", yaw);
        jsonObject.addProperty("world", super.getWorldName());
        return jsonObject;
    }

    public int getYaw() {
        return yaw;
    }

    @Override
    public Location getLocation() {
        return new Location(Bukkit.getWorld(super.getWorldName()), super.getX(), super.getY(), super.getZ(), yaw, 0);
    }
}
