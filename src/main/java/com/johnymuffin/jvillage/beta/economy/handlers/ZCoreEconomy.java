package com.johnymuffin.jvillage.beta.economy.handlers;

import com.johnymuffin.jvillage.beta.economy.EconomyHandler;
import com.johnymuffin.jvillage.beta.economy.TransactionResult;
import me.zavdav.zcore.ZCore;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class ZCoreEconomy implements EconomyHandler {

    @Override
    public TransactionResult addMoney(Player player, double amount) {
        try {
            ZCore.getOfflinePlayer(player.getUniqueId()).getAccount().add(BigDecimal.valueOf(amount));
            return TransactionResult.SUCCESS;
        } catch (Exception e) {
            return TransactionResult.UNKNOWN_ERROR;
        }
    }

    @Override
    public TransactionResult subtractMoney(Player player, double amount) {
        try {
            if (ZCore.getOfflinePlayer(player.getUniqueId()).getAccount().subtract(BigDecimal.valueOf(amount))) {
                return TransactionResult.SUCCESS;
            } else {
                return TransactionResult.INSUFFICIENT_FUNDS;
            }
        } catch (Exception e) {
            return TransactionResult.UNKNOWN_ERROR;
        }
    }

}
