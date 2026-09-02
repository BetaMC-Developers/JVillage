package com.johnymuffin.jvillage.beta.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.interfaces.ClaimManager;
import com.johnymuffin.jvillage.beta.models.chunk.ChunkClaimSettings;
import com.johnymuffin.jvillage.beta.models.chunk.VChunk;
import com.johnymuffin.jvillage.beta.models.chunk.VClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.UUID;

import static com.johnymuffin.jvillage.beta.models.VillageRanks.*;
import static com.johnymuffin.jvillage.beta.models.VillageOwnerTitles.*;

public class Village implements ClaimManager {
    private final JVillage plugin;
    private String townName;
    private final UUID townUUID;
    private final ArrayList<UUID> members = new ArrayList<UUID>();
    private final ArrayList<UUID> assistants = new ArrayList<UUID>();
    private UUID owner;
    private VSpawnCords townSpawn;
    private TreeMap<String, VSpawnCords> warps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private boolean modified = false;

    private final HashMap<VillageFlags, Boolean> flags = new HashMap<VillageFlags, Boolean>();

    private final ArrayList<UUID> invited = new ArrayList<UUID>();

    private final ArrayList<ChunkClaimSettings> claimMetadata = new ArrayList<>();

    private final long creationTime;

    private double balance;

    public VillageRanks rank;
    private String rankName;

    public VillageOwnerTitles ownerTitle;
    private String ownerTitleName;

    private void initializeFlags() {
        for (VillageFlags flag : VillageFlags.values()) {
            flags.put(flag, false);
        }
    }

    private void initializeRank(Village village) {
        //Amount of people in the village
        int memberCount = village.getMembers().length + village.getAssistants().length + 1; //The 1 is added for the owner
        if (memberCount >= 1) {
            village.rank = Tribe;
            village.ownerTitle = Chief;
        }
        if (memberCount >= 11 && memberCount < 21) {
            village.rank = Settlement;
            village.ownerTitle = Leader;
        }
        if (memberCount >= 21 && memberCount < 51) {
            village.rank = Town;
            village.ownerTitle = Mayor;
        }
        if (memberCount >= 51 && memberCount < 101) {
            village.rank = City;
            village.ownerTitle = Mayor;
        }
        if (memberCount >= 101 && memberCount < 501) {
            village.rank = Metropolis;
            village.ownerTitle = Mayor;
        }
        if (memberCount >= 501 && memberCount < 1001) {
            village.rank = Megacity;
            village.ownerTitle = Mayor;
        }
        if (memberCount >= 1001) {
            village.rank = Ecumenopolis;
            village.ownerTitle = Mayor;
        }
    }

    public Village(JVillage plugin, String townName, UUID townUUID, UUID owner, VChunk vChunk, VSpawnCords townSpawn) {
        this.plugin = plugin;
        this.townName = townName;
        this.townUUID = townUUID;
        this.owner = owner;
        System.out.println("[JVillage Debug] Claiming initial chunk: " + addClaim(new VClaim(this, vChunk)));
        this.townSpawn = townSpawn;
        modified = true;
        this.creationTime = System.currentTimeMillis() / 1000L;
        initializeFlags();
        initializeRank(this);
    }

    //Create village from JSON
    public Village(JVillage plugin, UUID uuid, JsonObject object) {
        this.plugin = plugin;
        this.townName = object.get("name").getAsString();
        this.townUUID = uuid; // Ignore UUID in JSON file and use the one from the file name
        this.owner = UUID.fromString(object.get("owner").getAsString());
        this.townSpawn = new VSpawnCords(object.get("townSpawn").getAsJsonObject());
        JsonObject warps = object.has("warps") ? object.get("warps").getAsJsonObject() : new JsonObject();
        for (String warpName : warps.keySet()) {
            VSpawnCords cords = new VSpawnCords(warps.get(warpName).getAsJsonObject());
            this.warps.put(warpName, cords);
        }

        JsonArray members = object.get("members").getAsJsonArray();
        for (JsonElement member : members) {
            this.members.add(UUID.fromString(member.getAsString()));
        }
        JsonArray assistants = object.get("assistants").getAsJsonArray();
        for (JsonElement assistant : assistants) {
            this.assistants.add(UUID.fromString(assistant.getAsString()));
        }
        JsonArray invited = object.has("invited") ? object.get("invited").getAsJsonArray() : new JsonArray();
        for (JsonElement invitee : invited) {
            this.invited.add(UUID.fromString(invitee.getAsString()));
        }

        JsonArray claims = object.get("claims").getAsJsonArray();
        //Loop through worlds
        for (JsonElement claim : claims) {
            JsonArray worldClaims = claim.getAsJsonArray();
            String worldName = worldClaims.get(0).getAsString();
            worldClaims.remove(0); //Remove world name from arrays

            //Skip to next world if world is not loaded
            //TODO: Add support for dynamically loading worlds
            if (Bukkit.getWorld(worldName) == null) {
                continue;
            }


            //Loop through claims in each world
            for (JsonElement worldClaim : worldClaims) {
                JsonArray claimCords = worldClaim.getAsJsonArray();
                int x = claimCords.get(0).getAsInt();
                int z = claimCords.get(1).getAsInt();
//                VChunk vChunk = new VChunk(worldName, x, z);
                VClaim vClaim = new VClaim(this.getTownUUID(), worldName, x, z);
//                if (this.plugin.isClaimed(vClaim)) {
//                    Village village = this.plugin.getVillageAtLocation(vClaim);
//                    plugin.logger(Level.WARNING, "Skipping claim: " + vClaim.toString() + " for " + getTownName() + " as it is already claimed by " + village.getTownName() + ". It is advised that you delete this claim from the JSON file or unclaim it with \"/va village unclaim\" while standing in it.");
//                    //Possibly a continued here? For now I'll leave it up to admins to fix
//                }
                addClaim(vClaim);
            }
            balance = object.has("balance") ? object.get("balance").getAsDouble() : 0.0;
            //rank = determineVillageRank(members.size());
        }

        //Load chunk claim metadata
        JsonArray chunkClaimMetadata = object.has("chunkClaimMetadata") ? object.get("chunkClaimMetadata").getAsJsonArray() : new JsonArray();
        for (JsonElement worldArrayRaw : chunkClaimMetadata) {
            JsonArray worldArray = worldArrayRaw.getAsJsonArray();

            String worldName = worldArray.get(0).getAsString();

            for (int i = 1; i < worldArray.size(); i++) {
                JsonObject chunkMetadata = worldArray.get(i).getAsJsonObject();
                ChunkClaimSettings settings = new ChunkClaimSettings(this, chunkMetadata, worldName);
                claimMetadata.add(settings);
            }

        }

        this.creationTime = object.has("creationTime") ? object.get("creationTime").getAsLong() : 1640995200L;


        initializeFlags();
        //Load flags saved
        JsonObject flags = object.has("flags") ? object.get("flags").getAsJsonObject() : new JsonObject();
        for (String flag : flags.keySet()) {
            this.flags.put(VillageFlags.valueOf(flag), flags.get(flag).getAsBoolean());
        }

        initializeRank(this);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void removeChunkClaimSettings(ChunkClaimSettings settings) {
        modified = true;
        claimMetadata.remove(settings);
    }

    public ChunkClaimSettings getChunkClaimSettings(VChunk vChunk) {
        ChunkClaimSettings claim = null;

        //Attempt to find metadata for chunk
        int index = claimMetadata.indexOf(vChunk);
        if (index != -1) {
            claim = claimMetadata.get(index);
        }

        //Generate new Metadata if it doesn't exist
        if (claim == null) {
            //Time of 1st of January 2023
            long time = 1640995200L;
            claim = new ChunkClaimSettings(this, time, getOwner(), vChunk, 0);
            claimMetadata.add(claim);
        }

        return claim;
    }

    public void addChunkClaimMetadata(ChunkClaimSettings settings) {
        modified = true;
        //Remove old metadata if it exists
        claimMetadata.remove(settings); //This works because it extends VChunk
        claimMetadata.add(settings);
    }


    public JsonObject getJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("name", this.townName);
        object.addProperty("owner", this.owner.toString());
        JsonArray members = new JsonArray();
        for (UUID member : this.members) {
            members.add(member.toString());
        }
        object.add("members", members);
        JsonArray assistants = new JsonArray();
        for (UUID assistant : this.assistants) {
            assistants.add(assistant.toString());
        }
        object.add("assistants", assistants);
        JsonArray invited = new JsonArray();
        for (UUID invitee : this.invited) {
            invited.add(invitee.toString());
        }
        object.add("invited", invited);
        JsonArray claimsJsonArray = new JsonArray();
        for (String worldName : this.getWorldsWithClaims()) {
            JsonArray worldClaims = new JsonArray();
            worldClaims.add(worldName);
            for (VClaim vClaim : this.getClaimsInWorld(worldName)) {
                JsonArray claimCords = new JsonArray();
                claimCords.add(vClaim.getX());
                claimCords.add(vClaim.getZ());
                worldClaims.add(claimCords);
            }
            claimsJsonArray.add(worldClaims);
        }

        //Save chunk claim metadata
        JsonArray chunkClaimMetadata = new JsonArray();
        for (String worldName : this.getWorldsWithClaims()) {
            JsonArray worldArray = new JsonArray();
            worldArray.add(worldName);
            for (ChunkClaimSettings settings : this.getChunkClaimSettingsInWorld(worldName)) {
                worldArray.add(settings.getJsonObject());
            }
            chunkClaimMetadata.add(worldArray);
        }
        object.add("chunkClaimMetadata", chunkClaimMetadata);

        //Save Flags
        JsonObject flags = new JsonObject();
        for (VillageFlags flag : this.flags.keySet()) {
            flags.addProperty(flag.toString(), this.flags.get(flag));
        }
        object.add("flags", flags);

        object.add("claims", claimsJsonArray);
        object.add("townSpawn", this.townSpawn.getJsonObject());
        object.addProperty("creationTime", this.creationTime);
        object.addProperty("balance", this.balance);

        JsonObject warps = new JsonObject();
        for (Map.Entry<String, VSpawnCords> entry : this.warps.entrySet()) {
            warps.add(entry.getKey(), entry.getValue().getJsonObject());
        }
        object.add("warps", warps);
        return object;
    }

    private ChunkClaimSettings[] getChunkClaimSettingsInWorld(String worldName) {
        ArrayList<ChunkClaimSettings> settings = new ArrayList<>();
        for (ChunkClaimSettings setting : claimMetadata) {
            if (setting.getWorldName().equals(worldName)) {
                settings.add(setting);
            }
        }
        return settings.toArray(new ChunkClaimSettings[0]);
    }

    public void invitePlayer(UUID uuid) {
        this.invited.add(uuid);
    }

    public void uninvitePlayer(UUID uuid) {
        this.invited.remove(uuid);
    }

    public boolean isInvited(UUID uuid) {
        return this.invited.contains(uuid);
    }


    public boolean addClaim(VClaim vChunk) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        return plugin.addClaim(vChunk);
    }

    public boolean removeClaim(VClaim vChunk) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        return plugin.removeClaim(vChunk);
    }

    public boolean isClaimed(VChunk vChunk) {
        return plugin.isClaimed(vChunk);
    }


//    public Village(JSONObject jsonObject) {
//        townName = (String) jsonObject.get("name");
//        townUUID = UUID.fromString((String) jsonObject.get("uuid"));
//        //Load Member List
//        for (Object member : (JSONArray) jsonObject.get("members")) {
//            this.members.add(UUID.fromString((String) member));
//        }
//        //Load Assistant List
//        for (Object assistant : (JSONArray) jsonObject.get("assistants")) {
//            this.assistants.add(UUID.fromString((String) assistant));
//        }
//        this.owner = UUID.fromString((String) jsonObject.get("owner"));
//        //Load Claims
//        JSONArray claims = (JSONArray) jsonObject.get("claims");
//        for (Object worldObject : claims) {
//            JSONObject world = (JSONObject) worldObject;
//            String worldName = (String) world.get("world");
//            JSONArray worldClaims = (JSONArray) world.get("claims");
//            for (Object claimObject : worldClaims) {
//                String claim = (String) claimObject;
//                String cords[] = claim.split(".");
//                VChunk vChunk = new VChunk(worldName, Integer.parseInt(cords[0]), Integer.parseInt(cords[1]));
//                claims.add(vChunk);
//            }
//        }
//        //Load Town Spawn
//        JSONObject townSpawn = (JSONObject) jsonObject.get("townSpawn");
//        this.townSpawn = new VCords(Integer.parseInt((String) townSpawn.get("x")), Integer.parseInt((String) townSpawn.get("y")), Integer.parseInt((String) townSpawn.get("z")), (String) townSpawn.get("world"));
//
//    }

    public void setFlag(VillageFlags flag, boolean value) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        flags.put(flag, value);
    }

    public boolean canPlayerAlter(Player player) {
        if (isRandomCanAlter()) {
            return true;
        }
        if (isMember(player.getUniqueId())) {
            return true;
        }
        return false;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
        modified = true;
    }

    public UUID getTownUUID() {
        return townUUID;
    }

    public UUID[] getMembers() {
        return members.toArray(new UUID[members.size()]);
    }


    public void addMember(UUID uuid) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        members.add(uuid);
        initializeRank(this);
    }

    public UUID[] getAssistants() {
        return assistants.toArray(new UUID[assistants.size()]);
    }

    public void removeAssistant(UUID uuid) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        assistants.remove(uuid);

        //Remove from members if they are in there
        members.remove(uuid);
    }

    public void addAssistant(UUID uuid) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        assistants.add(uuid);

        //Remove from members if they are in there
        members.remove(uuid);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        owner = uuid;

        //Remove the new owner from the members list or assistants list
        //TODO: This shouldn't be handled in this low level function. Move later
        if (members.contains(uuid)) {
            members.remove(uuid);
        }

        if (assistants.contains(uuid)) {
            assistants.remove(uuid);
        }
    }

//    public HashMap<String, ArrayList<VChunk>> getClaims() {
//        return claims;
//    }

    public VSpawnCords getTownSpawn() {
        return townSpawn;
    }

    public void setTownSpawn(VSpawnCords cords) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        townSpawn = cords;
    }

    public TreeMap<String, VSpawnCords> getWarps() {
        return this.warps;
    }

    public void addWarp(String name, VSpawnCords cords) {
        modified = true;
        warps.put(name, cords);
    }

    public void removeWarp(String name) {
        modified = true;
        warps.remove(name);
    }

    public boolean isMember(UUID uuid) {
        if (members.contains(uuid)) {
            return true;
        }
        if (assistants.contains(uuid)) {
            return true;
        }

        if (owner.equals(uuid)) {
            return true;
        }

        return false;
    }

    public boolean isAssistant(UUID uuid) {
        if (assistants.contains(uuid)) {
            return true;
        }

        if (owner.equals(uuid)) {
            return true;
        }

        return false;
    }

    public boolean isOwner(UUID uuid) {
        if (owner.equals(uuid)) {
            return true;
        }

        return false;
    }

    public boolean removeMember(UUID uuid) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        return members.remove(uuid);
    }

    public boolean removePlayerFromVillage(UUID uuid) {
        if (isOwner(uuid)) {
            throw new IllegalArgumentException("Cannot remove owner from village");
        }
        if (isAssistant(uuid)) {
            removeAssistant(uuid);
            return true;
        }
        if (isMember(uuid)) {
            removeMember(uuid);
            return true;
        }
        return false;
    }

    public List<VClaim> getClaims() {
        return plugin.getVillageClaimsArray(this);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.modified = modified;
    }


    public boolean isRandomCanAlter() {
        return this.flags.get(VillageFlags.RANDOM_CAN_ALTER);
    }

    public void setRandomCanAlter(boolean randomCanAlter) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.flags.put(VillageFlags.RANDOM_CAN_ALTER, randomCanAlter);
    }

    public boolean isMobsCanSpawn() {
        return this.flags.get(VillageFlags.MOBS_CAN_SPAWN);
    }

    public void setMobsCanSpawn(boolean mobsCanSpawn) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.flags.put(VillageFlags.MOBS_CAN_SPAWN, mobsCanSpawn);
    }

    public boolean isMobSpawnerBypass() {
        return this.flags.get(VillageFlags.MOB_SPAWNER_BYPASS);
    }

    public boolean isPvpEnabled() {
        return this.flags.get(VillageFlags.PVP_ENABLED);
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.flags.put(VillageFlags.PVP_ENABLED, pvpEnabled);
    }

    public void setMobSpawnerBypass(boolean mobSpawnerBypass) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.flags.put(VillageFlags.MOB_SPAWNER_BYPASS, mobSpawnerBypass);
    }

    public boolean isMembersCanInvite() {
        return this.flags.get(VillageFlags.MEMBERS_CAN_INVITE);
    }

    public void setMembersCanInvite(boolean membersCanInvite) {
        modified = true; // Indicate that the village has been modified and needs to be saved
        this.flags.put(VillageFlags.MEMBERS_CAN_INVITE, membersCanInvite);
    }

    public boolean isPreventMushroomSpread() {return this.flags.getOrDefault(VillageFlags.PREVENT_MUSHROOM_SPREAD, false);
    }

    public void setPreventMushroomSpread(boolean value) {
        modified = true;
        this.flags.put(VillageFlags.PREVENT_MUSHROOM_SPREAD, value);
    }

    public boolean isPreventIceSnowMelt() {return this.flags.getOrDefault(VillageFlags.PREVENT_ICE_SNOW_MELT, false);
    }

    public void setPreventIceSnowMelt(boolean value) {
        modified = true;
        this.flags.put(VillageFlags.PREVENT_ICE_SNOW_MELT, value);
    }

    public int getTotalClaims() {
        return getClaims().size();
    }

    public void broadcastToTown(String message) {
        String broadcastMessage = ChatColor.GOLD + "[" + ChatColor.AQUA + "Village: " + getTownName() + ChatColor.GOLD + "] " + ChatColor.GRAY + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMember(player.getUniqueId())) {
                player.sendMessage(broadcastMessage);
            }
        }
    }

    public String[] getWorldsWithClaims() {
        ArrayList<String> worlds = new ArrayList<>();
        for (VClaim vClaim : getClaims()) {
            if (!worlds.contains(vClaim.getWorldName())) {
                worlds.add(vClaim.getWorldName());
            }
        }
        return worlds.toArray(new String[worlds.size()]);
    }

    private VClaim[] getClaimsInWorld(String world) {
        ArrayList<VClaim> vClaims = new ArrayList<>();
        for (VClaim vClaim : getClaims()) {
            if (vClaim.getWorldName().equalsIgnoreCase(world)) {
                vClaims.add(vClaim);
            }
        }
        return vClaims.toArray(new VClaim[vClaims.size()]);
    }

    public HashMap<VillageFlags, Boolean> getFlags() {
        return flags;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        modified = true; // Indicate that the village has been modified and needs to be saved
    }

    public void addBalance(double amount) {
        if(amount < 0) throw new IllegalArgumentException("Amount must be positive");
        setBalance(getBalance() + amount);
    }

    public void subtractBalance(double amount) {
        if(amount < 0) throw new IllegalArgumentException("Amount must be positive");
        setBalance(getBalance() - amount);
    }

    public boolean hasEnough(double amount) {
        return this.balance >= amount;
    }
}
