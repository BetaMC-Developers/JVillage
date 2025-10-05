package com.johnymuffin.jvillage.beta.economy;

public class JVillageEconomy {

    private static EconomyHandler handler;

    public static boolean isEconomyEnabled() {
        return handler != null;
    }

    public static EconomyHandler getHandler() {
        return handler;
    }

    public static void setHandler(EconomyHandler handler) {
        JVillageEconomy.handler = handler;
    }

}
