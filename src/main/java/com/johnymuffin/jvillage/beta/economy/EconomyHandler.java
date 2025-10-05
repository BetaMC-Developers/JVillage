package com.johnymuffin.jvillage.beta.economy;

import org.bukkit.entity.Player;

public interface EconomyHandler {

    TransactionResult addMoney(Player player, double amount);

    TransactionResult subtractMoney(Player player, double amount);

}
