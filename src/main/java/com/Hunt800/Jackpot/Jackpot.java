package com.Hunt800.Jackpot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
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
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * Jackpot for Bukkit
 *
 * @author Hunt800
 */
public class Jackpot extends JavaPlugin {
    private final JackpotPlayerListener playerListener = new JackpotPlayerListener(this);
    private final JackpotBlockListener blockListener = new JackpotBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public static PermissionHandler Permissions;
    private static final File FileP = new File("plugins/Jackpot/config.properties");
    private static final String FileDir = "plugins/Jackpot";
    private Integer defaultDistance = 25;
    private Integer defaultDiceSize = 6;
    
   
    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
    	setupPermissions(); //Loads permissions crap
    	loadProperties();
        // Register our events
        PluginManager pm = getServer().getPluginManager();
       

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription(); //For getting plugin info
        System.out.println( "["+ pdfFile.getName() + "] version [" + pdfFile.getVersion() + "]: Enabled!" ); //Prints to server
    }
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] split = args; //Arguments (sep. by spaces) are put into this array
        String cmdName = command.getName().toLowerCase(); //Actual command (ie, with "/roll 1 6" this is "/roll"
        String diceValue = ""; //For what to print when diceValue is determined
        Integer sides = defaultDiceSize; //Default dice sides
        Integer diceNum = 1; //Default dice number
            if (sender instanceof Player) { //Only execute if the sender was a player
                Player source = (Player) sender; //Gets the player who sent the command
                
                //Following block is for rolling dice
                if(Permissions.has(source, "jackpot.roll.local") && cmdName.equals("roll")) {
	                    if (split.length == 0) {
	                        diceValue = rollDie(defaultDiceSize,1);
	                    } else {
	                    	diceNum = Integer.parseInt(split[0]);
	                    	sides = Integer.parseInt(split[1]);
	                    	if(diceNum < 1 || diceNum > 10) {
	                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: You can't roll less than " + ChatColor.WHITE + "1 " + ChatColor.RED + "or more than " + ChatColor.WHITE + "10 " + ChatColor.RED + "dice at a time! Setting [diceNum] to [1]");
	                    		diceNum = 1;
	                    	}
	                    	if(sides < 2) {
	                    		sides = 2;
	                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: A die cannot have less than 2 sides! Setting [sides] to [2]");
	                    	}
	                    	diceValue = rollDie(sides, diceNum);
	                    }
	                    boolean hasBroadcasted = false;
	                    String result = source.getDisplayName() + ChatColor.GOLD + " rolled " + ChatColor.WHITE + diceNum + ChatColor.GOLD + " " + ChatColor.WHITE + sides + ChatColor.GOLD + "-sided dice and got: " + ChatColor.RED + diceValue;
	                    if(split.length == 3) {
	                    	if(split[2].toLowerCase().equals("g") && Permissions.has(source, "jackpot.roll.global")) {
	                    		hasBroadcasted = true;
	                    		getServer().broadcastMessage(result);
	                    	}
	                    }
	                    if(!hasBroadcasted) {
	                    	broadcastNear(source, result, defaultDistance);
	                    }
	                    System.out.println("[Jackpot] " + source.getName() + " rolled " + diceNum + " " + sides + "-sided dice and got: " + diceValue);
                } else {
                	if(cmdName.equals("roll")){
                		source.sendMessage(ChatColor.RED + "You do not have permission to roll dice!");
                	}
                }
                
                //Following block is for flipping coins
                if(Permissions.has(source, "jackpot.flip.local") && cmdName.equals("cointoss")) {
                    if (split.length == 0) {
                        diceValue = flipCoin(1);
                    } else {
                    	diceNum = Integer.parseInt(split[0]);
                    	if(diceNum < 1 || diceNum > 10) {
                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: You can't flip less than " + ChatColor.WHITE + "1 " + ChatColor.RED + "or more than " + ChatColor.WHITE + "10 " + ChatColor.RED + "coins at a time! Setting [coinNum] to [1]");
                    		diceNum = 1;
                    	}
                    	diceValue = flipCoin(diceNum);
                    }
                    boolean hasBroadcasted = false;
                    String result = source.getDisplayName() + ChatColor.GOLD + " flipped " + ChatColor.WHITE + diceNum + ChatColor.GOLD + " coin(s) and got: " + ChatColor.RED + diceValue;
                    if(split.length == 2) {
                    	if(split[1].toLowerCase().equals("g") && Permissions.has(source, "jackpot.flip.global")) {
                    		hasBroadcasted = true;
                    		getServer().broadcastMessage(result);
                    	}
                    }
                    if(!hasBroadcasted) {
                    	broadcastNear(source, result, defaultDistance);
                    }
                    System.out.println("[Jackpot] " + source.getName() + " flipped " + diceNum + " coin(s) and got: " + diceValue);
            } else {
            	if(cmdName.equals("cointoss")){
            		source.sendMessage(ChatColor.RED + "You do not have permission to flip coins!");
            	}
            }
            }
            return true;
    }
    private void loadProperties() {
    	Properties prop = new Properties();
    	if(FileP.exists()) {
    	try {
        	FileInputStream in = new FileInputStream(FileP);
    		//load a properties file
    		prop.load(in);
    		
    		//Set properties
    		defaultDistance = Integer.parseInt((prop.getProperty("broadcast-distance")));
    		defaultDiceSize = Integer.parseInt((prop.getProperty("default-dice-size")));
    	} catch (IOException ex) { ex.printStackTrace(); }
    	} else {
    		try {
    			try {
    				boolean success = (new File(FileDir)).mkdirs();
    			    if (success) {
    			      System.out.println("Directories: " + FileDir + " created");
    			    }

    			    }catch (Exception e){//Catch exception if any
    			      System.err.println("Error: " + e.getMessage());
    			    }
    			FileP.createNewFile();
    			FileOutputStream out = new FileOutputStream(FileP);
        		prop.setProperty("broadcast-distance", "25");
        		prop.setProperty("default-dice-size", "6");
     
        		//save properties to project root folder
        		prop.store(out, null);
        		
        		System.out.println("[Jackpot] Generating config.properties");
     
        	} catch (IOException ex) { ex.printStackTrace(); }
    	}
    }
    private void broadcastNear(Player player, String toBroadcast, Integer distance) {
        for (Player p : getServer().getOnlinePlayers()) {
            if (getDistance(p.getLocation(), player.getLocation()) <= distance) {
                p.sendMessage(toBroadcast);
            }
        }
    }
    private float getDistance(Location p, Location q) {
        return (float) Math.sqrt(Math.pow(p.getBlockX() - q.getBlockX(), 2) + Math.pow(p.getBlockY() - q.getBlockY(), 2) + Math.pow(p.getBlockZ() - q.getBlockZ(), 2));
    }
    private String rollDie(Integer sides, Integer dice) {
    	Random generator = new Random();
    	String dV = "";
    	for(Integer i = 0; i < dice; i++) {
    		Integer r = generator.nextInt(sides) + 1;
    		if(i < dice - 1) {
    			dV = dV + r + ", ";
    		} else {
    			dV = dV + r + ".";
    		}
    	}
    	return dV;
    }
    private String flipCoin(Integer numCoins) {
    	Random generator = new Random();
    	String dV = "";
    	for(Integer i = 0; i < numCoins; i++) {
    		Integer r = generator.nextInt(2);
    		String r2 = "<Heads>";
    		
    		if(r == 0) {
    			r2 = "<Tails>";
    		}
    		if(i < numCoins - 1) {
    			dV = dV + r2 + ", ";
    		} else {
    			dV = dV + r2 + ".";
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
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.Permissions == null) {
            if (test != null) {
                this.Permissions = ((Permissions)test).getHandler();
            } else {
                System.out.println("[Jackpot] Permissions not detected, defaulting to Ops-Only");
            }
        }
    }
}

