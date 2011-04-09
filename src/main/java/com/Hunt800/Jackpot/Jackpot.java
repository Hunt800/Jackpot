package com.Hunt800.Jackpot;

import java.io.File;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.*;

/**
 * Jackpot for Bukkit
 *
 * @author Hunt800
 */
public class Jackpot extends JavaPlugin {
    private final JackpotPlayerListener playerListener = new JackpotPlayerListener(this);
    private final JackpotBlockListener blockListener = new JackpotBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
   
    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
        PluginManager pm = getServer().getPluginManager();
       

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "["+ pdfFile.getName() + "] version [" + pdfFile.getVersion() + "]: Enabled!" );
    }
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] split = args;
        String cmdName = command.getName().toLowerCase();
        String diceValue = "";
        Integer sides = 6;
        Integer diceNum = 1;
            if (sender instanceof Player) {
                Player source = (Player) sender;
                if (cmdName.equals("roll")) {
                    if (split.length == 0) {
                        diceValue = rollDie(6,1);
                    } else {
                    	diceNum = Integer.parseInt(split[0]);
                    	sides = Integer.parseInt(split[1]);
                    	if(diceNum < 1 || diceNum > 10) {
                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: You can't roll less than " + ChatColor.WHITE + "1 " + ChatColor.RED + "or more than" + ChatColor.WHITE + "10 " + ChatColor.RED + "dice at a time! Setting [diceNum] to [1]");
                    		diceNum = 1;
                    	}
                    	if(sides < 2) {
                    		sides = 2;
                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: A die cannot have less than 2 sides! Setting [sides] to [2]");
                    	}
                    	diceValue = rollDie(sides, diceNum);
                    }
                    String playerName = "" + source;
                    playerName = playerName.substring(17,playerName.length() - 1);
                    getServer().broadcastMessage(ChatColor.GOLD + playerName + " rolled " + ChatColor.WHITE + diceNum + ChatColor.GOLD + " " + ChatColor.WHITE + sides + ChatColor.GOLD + "-sided dice and got: " + ChatColor.RED + diceValue);
                    System.out.println("[Jackpot] " + playerName + " rolled " + diceNum + " " + sides + "-sided dice and got: " + diceValue);
                }

            }
            return true;
    }
    private String rollDie(Integer sides, Integer dice) {
    	Random generator = new Random();
    	String dV = "";
    	for(Integer i = 0; i < dice; i++) {
    		Integer r = generator.nextInt(sides - 1) + 1;
    		if(i < dice - 1) {
    			dV = dV + r + ", ";
    		} else {
    			dV = dV + r + ".";
    		}
    	}
    	return dV;
    }
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
    	PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "["+ pdfFile.getName() + "] version [" + pdfFile.getVersion() + "]: Disabled!" );
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}

