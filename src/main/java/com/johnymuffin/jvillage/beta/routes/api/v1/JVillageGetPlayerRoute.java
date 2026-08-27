package com.johnymuffin.jvillage.beta.routes.api.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.player.VPlayer;
import com.johnymuffin.jvillage.beta.routes.JVillageNormalRoute;
import org.bukkit.Bukkit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class JVillageGetPlayerRoute extends JVillageNormalRoute {

    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        if (request.getParameter("uuid") == null) {
            this.returnError(response, "No UUID field provided");
            return;
        }
        final String uuid = request.getParameter("uuid");
        Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        if (!p.matcher(uuid).matches()) {
            this.returnError(response, "Invalid UUID provided");
            return;
        }

        //Change to async
        final AsyncContext ctxt = request.startAsync();
        ctxt.start(() -> {
            //Change to Bukkit Synchronised Task
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.jVillage, () -> {
                try {
                    UUID playerUUID = UUID.fromString(uuid);
                    JsonObject playerJSON = new JsonObject();
                    if (!jVillage.getPlayerData().isPlayerKnown(playerUUID)) {
                        playerJSON.addProperty("found", false);
                        playerJSON.addProperty("error", false);
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(playerJSON.toString());
                        ctxt.complete();
                        return;
                    }
                    playerJSON.addProperty("found", true);
                    VPlayer player = jVillage.getPlayerMap().getPlayer(playerUUID);

                    playerJSON.addProperty("name", player.getUsername());
                    playerJSON.addProperty("uuid", player.getUUID().toString());

                    JsonObject villageJSON = new JsonObject();

                    //Get all villages player owns
                    JsonArray villagesOwned = new JsonArray();
                    for (Village village : player.getTownsOwned()) {
                        villagesOwned.add(village.getTownUUID().toString());
                    }
                    villageJSON.add("owner", villagesOwned);

                    //Get all villages player assists
                    JsonArray villagesAssisted = new JsonArray();
                    for (Village village : player.getTownsAssistantOf()) {
                        villagesAssisted.add(village.getTownUUID().toString());
                    }
                    villageJSON.add("assistant", villagesAssisted);

                    //Get all villages player is a member of
                    JsonArray villagesMember = new JsonArray();
                    for (Village village : player.getTownsMemberOf()) {
                        villagesMember.add(village.getTownUUID().toString());
                    }
                    villageJSON.add("member", villagesMember);

                    playerJSON.add("villages", villageJSON);

                    playerJSON.addProperty("error", false);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(playerJSON.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctxt.complete();
            }, 0L);
        });
    }

}
