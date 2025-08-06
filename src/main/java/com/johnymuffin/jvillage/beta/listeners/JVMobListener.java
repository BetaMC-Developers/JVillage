package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.player.VPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftSlime;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
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

public class JVMobListener extends EntityListener implements Listener {
    private JVillage plugin;

    public JVMobListener(JVillage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE_BY_ENTITY, this, Event.Priority.Normal, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onMobSpawnEvent(final CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(event.getEntity() instanceof Monster || entity instanceof CraftSlime)) {
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

        Player victimPlayer = (Player) event.getEntity();
        VPlayer vVictimPlayer = plugin.getPlayerMap().getPlayer(victimPlayer.getUniqueId());

        if (damager instanceof Wolf) {
            Wolf wolf = (Wolf) damager;
            wolf.setTarget(null);
        }

        //If the damager is a hostile mob, continue; Otherwise, whether the attack is allowed is checked
        if (!(damager instanceof Monster)) {

            if (damager instanceof CraftArrow) {
                CraftArrow arrow = (CraftArrow) damager;
                Entity shooter = arrow.getShooter();

                if (shooter instanceof Player) {
                    Player damagerPlayer = (Player) shooter;
                    VPlayer vDamagerPlayer = plugin.getPlayerMap().getPlayer(damagerPlayer.getUniqueId());

                    // Always check if the victim is in a PvP-disabled village
                    if (vDamagerPlayer.isLocatedInVillage()) {
                        Village damagerVillage = plugin.getVillageAtLocation(damagerPlayer.getLocation());
                        String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", damagerVillage.getTownName());
                        damagerPlayer.sendMessage(message);
                        event.setCancelled(true);
                        return;
                    }

                    if (vVictimPlayer.isLocatedInVillage()) {
                        Village victimVillage = plugin.getVillageAtLocation(victimPlayer.getLocation());
                        if (!victimVillage.isPvpEnabled()) {
                            String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", victimVillage.getTownName());
                            damagerPlayer.sendMessage(message);
                            event.setCancelled(true);
                        }

                    }
                }
                return;
            }

            if (damager instanceof Player) {
                Player damagerPlayer = (Player) damager;
                VPlayer vDamagerPlayer = plugin.getPlayerMap().getPlayer(damagerPlayer.getUniqueId());

                boolean damagerInVillage = vDamagerPlayer.isLocatedInVillage();
                boolean victimInVillage = vVictimPlayer.isLocatedInVillage();

                if (damagerInVillage) {
                    Village victimVillage = plugin.getVillageAtLocation(victimPlayer.getLocation());
                    String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", victimVillage.getTownName());

                    damagerPlayer.sendMessage(message);
                    event.setCancelled(true);
                    return;
                }

                //noinspection ConstantValue
                if (!damagerInVillage && victimInVillage) {
                    Village victimVillage = plugin.getVillageAtLocation(victimPlayer.getLocation());
                    if (!victimVillage.isPvpEnabled()) {
                        String message = plugin.getLanguage().getMessage("pvp_denied").replace("%village%", victimVillage.getTownName());
                        damagerPlayer.sendMessage(message);
                        event.setCancelled(true);
                        return;
                    }
                }
                return;
            }
        }

        //Return if the player is not in a village
        if (!vVictimPlayer.isLocatedInVillage()) {
            return;
        }
//        System.out.println("Player was damaged by a hostile mob in a village");

        if (vVictimPlayer.getCurrentlyLocatedIn().isMobsCanSpawn()) {
            return;
        }

//        System.out.println("Player was damaged by a hostile mob in a village where mobs are not allowed to spawn");

        //Cancel the damage event if it is a hostile mob attacking a player in a village where mobs are not allowed to spawn
        event.setCancelled(true);

        if (!(damager instanceof Wolf) && damager instanceof Monster) {
            damager.teleport(damager.getLocation().subtract(0, 300, 0)); //Teleport hostile mob to void
        }
    }
}
