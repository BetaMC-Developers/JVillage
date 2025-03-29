package com.johnymuffin.jvillage.beta.commands.village;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.commands.JVBaseCommand;
import com.johnymuffin.jvillage.beta.models.District;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.player.VPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JDistrictCommand extends JVBaseCommand implements CommandExecutor {

    public JDistrictCommand(JVillage plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "jvillage.player.district")) {
            commandSender.sendMessage(language.getMessage("no_permission"));
            return true;
        }

        //Validate command sender is a player
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(language.getMessage("unavailable_to_console"));
            return true;
        }

        Player player = (Player) commandSender;
        VPlayer vPlayer = plugin.getPlayerMap().getPlayer(player.getUniqueId());
        Village village = vPlayer.getSelectedVillage();

        if (strings.length > 0) {
            String subcommand = strings[0];
            if (subcommand.equalsIgnoreCase("list")) {
                String message = language.getMessage("command_village_district_list");
                message = message.replace("%village%", village.getTownName());
                message = message.replace("%districts%", village.districts.toString());
                sendWithNewline(commandSender, message);
                return true;
            }
            if (subcommand.equalsIgnoreCase("create") || subcommand.equalsIgnoreCase("add")) {
                //Check if user is assistant or higher
                if (!village.isOwner(player.getUniqueId())) {
                    commandSender.sendMessage(language.getMessage("owner_or_higher"));
                    return true;
                }
                String district = strings[1];
                //Check if the district already exists
                if (village.districts.contains(district)) {
                    commandSender.sendMessage(language.getMessage("district_already_exists"));
                    return true;
                }
                //Create the district
                village.addDistrict(district);
                String message = language.getMessage("command_village_district_create");
                message = message.replace("%district%", district);
                message = message.replace("%village%", village.getTownName());
                sendWithNewline(commandSender, message);
                return true;
            }
            if (subcommand.equalsIgnoreCase("delete") || subcommand.equalsIgnoreCase("remove")) {
                //Check if user is assistant or higher
                if (!village.isOwner(player.getUniqueId())) {
                    commandSender.sendMessage(language.getMessage("owner_or_higher"));
                    return true;
                }
                String district = strings[1];
                //Check if the district even exists
                if (!(village.districts.contains(district))) {
                    commandSender.sendMessage(language.getMessage("district_does_not_exist"));
                    return true;
                }
                //Delete the district
                village.districts.remove(district);
                String message = language.getMessage("command_village_district_delete");
                message = message.replace("%district%", district);
                message = message.replace("%village%", village.getTownName());
                sendWithNewline(commandSender, message);
                return true;
            }

            if (subcommand.equalsIgnoreCase("here")) {
                String district = strings[1]; //needs to get district at location
                if (village.districts.contains(district)) {
                    sendWithNewline(commandSender, "District: " + district);
                }
                return true;
            }

            if (subcommand.equalsIgnoreCase("claim")) {
                String district = strings[1];
                String message = language.getMessage("command_village_district_claim");
                message = message.replace("%district%", district);
                message = message.replace("%village%", village.getTownName());
                sendWithNewline(commandSender, message);
                return true;
            }
        }

        return true;
    }
}
