package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.player.VPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class JVMobListener extends EntityListener implements Listener {
    private JVillage plugin;

    public JVMobListener(JVillage plugin) {
        this.plugin = plugin;
        //Bukkit.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE_BY_ENTITY, this, Event.Priority.Normal, plugin);
    }


    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onMobSpawnEvent(final CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        //See if the mob is in a village
        Village village = plugin.getVillageAtLocation(event.getLocation());
        if (village == null) {
            return;
        }

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER && village.isMobSpawnerBypass()) {
            //Allow the mob to spawn if it is from a spawner, and the village has mob spawner bypass enabled
            return;
        }

        if (village.isMobsCanSpawn()) {
            return;
        }
        //Cancel the spawn as the mob is in a village and mobs are not allowed to spawn
        event.setCancelled(true);
//        System.out.println("Blocked a hostile mob from spawning in a village");
    }


    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Normal)
    public void onEntityDamageEvent(final EntityDamageEvent preEvent) {

        if (!(preEvent instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) preEvent;

        CraftEntity damager = (CraftEntity) event.getDamager();

        //Return if the victim is not a player
        if (!(event.getEntity() instanceof CraftPlayer)) {
//            System.out.println("EntityDamageByEntityEvent: " + event.getEntity().toString());
            return;
        }
//        System.out.println("Player was damaged");

        //If the damager is a hostile mob, continue; Otherwise, whether the attack is allowed is checked
        if (!(damager instanceof Monster)) {

            if (damager instanceof CraftArrow) {
                CraftArrow arrow = (CraftArrow) event.getDamager();
                damager = (CraftEntity) arrow.getShooter();
                return;
            }

            if (damager instanceof Player) {

                //Determine if the damager is in a village
                Player damagerPlayer = (Player) damager;
                VPlayer vDamagerPlayer = plugin.getPlayerMap().getPlayer(damagerPlayer.getUniqueId());
                if (!vDamagerPlayer.isLocatedInVillage()) {
                    return;
                }
                Village damagerVillage = plugin.getVillageAtLocation(damager.getLocation());

                //Determine if the victim is in a village
                if (!(event.getEntity() instanceof Player)) {
                    if (event.getEntity() instanceof Wolf) {
                        Village wolfVillage = plugin.getVillageAtLocation(event.getEntity().getLocation());
                        if (!(wolfVillage.isPvpEnabled())) {
                            event.setCancelled(true);
                        }
                    }
                    return;
                }
                Player victimPlayer = (Player) event.getEntity();
                VPlayer vVictimPlayer = plugin.getPlayerMap().getPlayer(victimPlayer.getUniqueId());
                if (!vVictimPlayer.isLocatedInVillage()) {
                    return;
                }
                Village victimVillage = plugin.getVillageAtLocation(event.getEntity().getLocation());

                //Determine if pvp is allowed
                if (damagerVillage.isPvpEnabled() && victimVillage.isPvpEnabled()) {
                    return;
                }
                String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", damagerVillage.getTownName());
                damagerPlayer.sendMessage(message);
                event.setCancelled(true);
                return;
            }
        }
//        System.out.println("Player was damaged by a hostile mob");

        Player player = (Player) event.getEntity();
        VPlayer vPlayer = plugin.getPlayerMap().getPlayer(player.getUniqueId());

        //Return if the player is not in a village
        if (!vPlayer.isLocatedInVillage()) {
            return;
        }
//        System.out.println("Player was damaged by a hostile mob in a village");

        if (vPlayer.getCurrentlyLocatedIn().isMobsCanSpawn()) {
            return;
        }

//        System.out.println("Player was damaged by a hostile mob in a village where mobs are not allowed to spawn");

        //Cancel the damage event if it is a hostile mob attacking a player in a village where mobs are not allowed to spawn
        event.setCancelled(true);

        //Kill the hostile mob
        damager.teleport(damager.getLocation().subtract(0, 300, 0)); //Teleport the mob to the void
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Normal)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Wolf) {
            Village wolfVillage = plugin.getVillageAtLocation(event.getRightClicked().getLocation());
            if (wolfVillage.isPvpEnabled()) {
                return;
            }
            event.setCancelled(true);
        }
    }
}
