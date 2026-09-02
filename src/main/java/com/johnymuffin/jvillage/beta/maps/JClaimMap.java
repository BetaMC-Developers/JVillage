package com.johnymuffin.jvillage.beta.maps;

import com.johnymuffin.jvillage.beta.interfaces.ClaimManager;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.models.chunk.VChunk;
import com.johnymuffin.jvillage.beta.models.chunk.VClaim;
import com.legacyminecraft.poseidon.util.ChunkPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JClaimMap implements ClaimManager {

    private final Object2ObjectMap<UUID, List<VClaim>> claimsByVillage = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<World, Long2ObjectMap<VClaim>> worldClaims = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean addClaim(VClaim vChunk) {
        World world = Bukkit.getWorld(vChunk.getWorldName());
        if (world == null) {
            return false;
        }

        this.claimsByVillage.computeIfAbsent(vChunk.getVillage(), _ -> new ArrayList<>()).add(vChunk);
        long chunkKey = ChunkPos.of(vChunk.getX(), vChunk.getZ());
        this.worldClaims.computeIfAbsent(world, _ -> new Long2ObjectOpenHashMap<>()).put(chunkKey, vChunk);
        return true;
    }

    @Override
    public boolean removeClaim(VClaim vChunk) {
        World world = Bukkit.getWorld(vChunk.getWorldName());
        if (world == null) {
            return false;
        }

        this.claimsByVillage.computeIfAbsent(vChunk.getVillage(), _ -> new ArrayList<>()).remove(vChunk);
        long chunkKey = ChunkPos.of(vChunk.getX(), vChunk.getZ());
        this.worldClaims.computeIfAbsent(world, _ -> new Long2ObjectOpenHashMap<>()).remove(chunkKey);
        return true;
    }

    @Override
    public boolean isClaimed(VChunk vChunk) {
        World world = Bukkit.getWorld(vChunk.getWorldName());
        if (world == null) {
            return false;
        }

        return getClaimAtChunk(world, vChunk.getX(), vChunk.getZ()) != null;
    }

    public List<VClaim> getAllClaims() {
        return this.worldClaims.values().stream()
                .flatMap(claimByChunk -> claimByChunk.values().stream())
                .toList();
    }

    public List<VClaim> getClaimsInWorld(World world) {
        Long2ObjectMap<VClaim> claimByChunk = this.worldClaims.get(world);
        if (claimByChunk == null) {
            return null;
        }

        return claimByChunk.values().stream().toList();
    }

    public List<VClaim> getVillageClaims(Village village) {
        return this.claimsByVillage.computeIfAbsent(village.getTownUUID(), _ -> new ArrayList<>());
    }

    public VClaim getClaimAtChunk(World world, int chunkX, int chunkZ) {
        Long2ObjectMap<VClaim> claimByChunk = this.worldClaims.get(world);
        if (claimByChunk == null) {
            return null;
        }

        long chunkKey = ChunkPos.of(chunkX, chunkZ);
        return claimByChunk.get(chunkKey);
    }
}
