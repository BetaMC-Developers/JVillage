package com.johnymuffin.jvillage.beta.models;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class VCords {
    private int x;
    private int y;
    private int z;
    private String worldName;


    public VCords(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    public VCords(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    //Create from JSON
    public VCords(JsonObject jsonObject) {
        this.x = jsonObject.get("x").getAsInt();
        this.y = jsonObject.get("y").getAsInt();
        this.z = jsonObject.get("z").getAsInt();
        this.worldName = jsonObject.get("world").getAsString();
    }

    public JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
        jsonObject.addProperty("z", z);
        jsonObject.addProperty("world", worldName);
        return jsonObject;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof VCords)) return false;
        VCords cords = (VCords) obj;
        return Objects.equals(this.worldName, cords.worldName) && this.x == cords.x && this.y == cords.y && this.z == cords.z;
    }

    @Override
    public String toString() {
        return worldName + ": " + x + "," + y + "," + z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, worldName);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorldName() {
        return worldName;
    }
}
