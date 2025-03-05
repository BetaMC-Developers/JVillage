package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerListener;


public class JVEntityDamageListener implements Listener {
    private JVillage plugin;

    public JVEntityDamageListener(JVillage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE_BY_ENTITY, this, Event.Priority.Normal, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Highest)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {

        //Check if the entity being attacked is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Village damagerVillage = plugin.getVillageAtLocation(event.getDamager().getLocation());
        if (damagerVillage.isPvpEnabled) {
            return;
        }

        String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", damagerVillage.getTownName());
        Bukkit.getServer().broadcastMessage(message);
        event.setCancelled(true);
    }
}
