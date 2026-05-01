package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.models.chunk.VChunk;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.block.Block;

public class JVMushroomListener implements Listener {
    private final JVillage plugin;

    public JVMushroomListener(JVillage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Low)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block source = event.getSource();
        Block newBlock = event.getNewState().getBlock();

        if (source.getType().name().contains("MUSHROOM")) {
            VChunk vChunk = new VChunk(newBlock.getWorld().getName(), newBlock.getChunk().getX(), newBlock.getChunk().getZ());
            Village village = plugin.getVillageAtLocation(vChunk);
            if (village != null && village.isPreventMushroomSpread()) {
                event.setCancelled(true);
            }
        }
    }
}