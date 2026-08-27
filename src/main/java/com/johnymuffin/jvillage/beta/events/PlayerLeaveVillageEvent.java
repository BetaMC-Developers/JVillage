package com.johnymuffin.jvillage.beta.events;

import com.johnymuffin.jvillage.beta.models.Village;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveVillageEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Village village;

    private Player player;

    public PlayerLeaveVillageEvent(Player player, Village village) {
        super("PlayerLeaveVillageEvent");
        this.player = player;
        this.village = village;
    }

    public Player getPlayer() {
        return player;
    }

    private Village getVillage() {
        return village;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
