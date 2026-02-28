package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.models.chunk.VChunk;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class JVIceSnowListener implements Listener {

    private final JVillage plugin;

    public JVIceSnowListener(JVillage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Normal)
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        int id = block.getTypeId();
        if (id != 78 && id != 79) {
            return;
        }

        VChunk vChunk = new VChunk(
                block.getWorld().getName(),
                block.getChunk().getX(),
                block.getChunk().getZ()
        );

        Village village = plugin.getVillageAtLocation(vChunk);
        if (village != null && village.isPreventIceSnowMelt()) {
            event.setCancelled(true);
        }
    }
}