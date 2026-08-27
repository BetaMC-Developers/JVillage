package com.johnymuffin.jvillage.beta.routes.api.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.routes.JVillageNormalRoute;
import org.bukkit.Bukkit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class JVillageGetVillageList extends JVillageNormalRoute {

    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        //Change to async
        final AsyncContext ctxt = request.startAsync();
        ctxt.start(() -> {
            //Change to Bukkit Synchronised Task
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.jVillage, () -> {
                try {
                    JsonArray villageList = new JsonArray();
                    //Loop through all villages
                    for (UUID village : this.jVillage.getVillageMap().getKnownVillages()) {
                        Village villageObject = this.jVillage.getVillageMap().getVillage(village);
                        JsonObject villageJSON = new JsonObject();
                        villageJSON.addProperty("name", villageObject.getTownName());
                        villageJSON.addProperty("uuid", villageObject.getTownUUID().toString());
                        villageJSON.addProperty("owner", villageObject.getOwner().toString());
                        villageList.add(villageJSON);
                    }
                    JsonObject responseObject = new JsonObject();
                    responseObject.addProperty("error", false);
                    responseObject.add("villages", villageList);

                    //Send response
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println(responseObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctxt.complete();
            }, 0L);
        });
    }


}
