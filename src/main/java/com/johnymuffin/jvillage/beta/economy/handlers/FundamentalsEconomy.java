package com.johnymuffin.jvillage.beta.economy.handlers;

import com.johnymuffin.beta.fundamentals.api.FundamentalsAPI;
import com.johnymuffin.jvillage.beta.economy.EconomyHandler;
import com.johnymuffin.jvillage.beta.economy.TransactionResult;
import org.bukkit.entity.Player;

public class FundamentalsEconomy implements EconomyHandler {

    @Override
    public TransactionResult addMoney(Player player, double amount) {
        switch (FundamentalsAPI.getEconomy().additionBalance(player.getUniqueId(), amount)) {
            case successful:
                return TransactionResult.SUCCESS;
            default:
                return TransactionResult.UNKNOWN_ERROR;
        }
    }

    @Override
    public TransactionResult subtractMoney(Player player, double amount) {
        switch (FundamentalsAPI.getEconomy().additionBalance(player.getUniqueId(), amount)) {
            case successful:
                return TransactionResult.SUCCESS;
            case notEnoughFunds:
                return TransactionResult.INSUFFICIENT_FUNDS;
            default:
                return TransactionResult.UNKNOWN_ERROR;
        }
    }

}
