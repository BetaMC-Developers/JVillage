package com.johnymuffin.jvillage.beta.routes.api.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.models.VillageFlags;
import com.johnymuffin.jvillage.beta.routes.JVillageNormalRoute;
import org.bukkit.Bukkit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class JVillageGetVillageRoute extends JVillageNormalRoute {

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

                    UUID villageUUID = UUID.fromString(uuid);

                    Village village = this.jVillage.getVillageMap().getVillage(villageUUID);

                    JsonObject villageJSON = new JsonObject();

                    //Return error if village does not exist
                    if (village == null) {
                        villageJSON.addProperty("found", false);
                        villageJSON.addProperty("error", false);
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(villageJSON.toString());
                        ctxt.complete();
                        return;
                    }

                    villageJSON.addProperty("found", true);

                    villageJSON.addProperty("name", village.getTownName());
                    villageJSON.addProperty("uuid", village.getTownUUID().toString());
                    villageJSON.addProperty("owner", village.getOwner().toString());
                    //Get all assistants
                    JsonArray assistants = new JsonArray();
                    for (UUID assistant : village.getAssistants()) {
                        assistants.add(assistant.toString());
                    }
                    villageJSON.add("assistants", assistants);
                    //Get all members
                    JsonArray members = new JsonArray();
                    for (UUID member : village.getMembers()) {
                        members.add(member.toString());
                    }
                    villageJSON.add("members", members);

                    villageJSON.add("spawn", village.getTownSpawn().getJsonObject());
                    //Town Flags
                    JsonObject flags = new JsonObject();
                    for (VillageFlags flag : village.getFlags().keySet()) {
                        flags.addProperty(flag.toString(), village.getFlags().get(flag));
                    }
                    villageJSON.add("flags", flags);
                    villageJSON.addProperty("claims", village.getTotalClaims());
                    villageJSON.addProperty("error", false);

                    villageJSON.addProperty("creationTime", village.getCreationTime());

                    villageJSON.addProperty("balance", village.getBalance());

                    //Send response
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(villageJSON.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctxt.complete();
            }, 0L);
        });
    }

}
