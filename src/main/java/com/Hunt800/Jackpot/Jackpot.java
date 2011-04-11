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
import org.bukkit.event.Event.Type;
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
    private HashMap<Player, Deck> decks = new HashMap<Player, Deck>();
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
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

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
                if(Permissions.has(source, "jackpot.roll.local") && cmdName.equals("roll")) { //If player has the permission AND did /roll
	                    if (split.length == 0) { //Default roll (no args)
	                        diceValue = rollDie(defaultDiceSize,1);
	                    } else { //Non-default roll, multiple arguments
	                    	diceNum = Integer.parseInt(split[0]);
	                    	sides = Integer.parseInt(split[1]);
	                    	if(diceNum < 1 || diceNum > 10) { //In case user tries to roll less than 1 die or more than 10 (error/spam prevention)
	                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: You can't roll less than " + ChatColor.WHITE + "1 " + ChatColor.RED + "or more than " + ChatColor.WHITE + "10 " + ChatColor.RED + "dice at a time! Setting [diceNum] to [1]");
	                    		diceNum = 1; //Resets diceNum to 1 (default)
	                    	}
	                    	if(sides < 2) { //Less than 2 sides = pointless, this implements a minimum
	                    		sides = 2;
	                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: A die cannot have less than 2 sides! Setting [sides] to [2]");
	                    	}
	                    	diceValue = rollDie(sides, diceNum); //Once side/diceNum has been decided, this figures out the 'output value'
	                    }
	                    boolean hasBroadcasted = false; //Simple hack(ish) fix for making the global arg work
	                    String result = source.getDisplayName() + ChatColor.GOLD + " rolled " + ChatColor.WHITE + diceNum + ChatColor.GOLD + " " + ChatColor.WHITE + sides + ChatColor.GOLD + "-sided dice and got: " + ChatColor.RED + diceValue;
	                    if(split.length == 3) { //If there is a third argument...
	                    	if(split[2].toLowerCase().equals("g") && Permissions.has(source, "jackpot.roll.global")) { //...and it is "g" + the player has permission...
	                    		hasBroadcasted = true; //...let server know that the message has been broadcasted
	                    		getServer().broadcastMessage(result); //broadcast to the whole server
	                    	}
	                    }
	                    if(!hasBroadcasted) { //If the message has not been broadcasted (ie, globally)
	                    	broadcastNear(source, result, defaultDistance); //Then broadcast [STRING: result] to all players within [INTEGER: defaultDistance] of [PLAYER: source]
	                    }
	                    System.out.println("[Jackpot] " + source.getName() + " rolled " + diceNum + " " + sides + "-sided dice and got: " + diceValue);//Lets console know the result
                } else {//If the user doesn't have permission
                	if(cmdName.equals("roll")){ //If they user's command was roll
                		source.sendMessage(ChatColor.RED + "You do not have permission to roll dice!"); //Let them know they do not have permission
                	}
                }
                
                //Following block is for flipping coins
                if(Permissions.has(source, "jackpot.flip.local") && cmdName.equals("cointoss")) {//If command = /cointoss and user has permission
                    if (split.length == 0) { //No args, just flip 1 coin
                        diceValue = flipCoin(1); //Flip
                    } else { //If there are arguments
                    	diceNum = Integer.parseInt(split[0]); //The first argument is number of coins
                    	if(diceNum < 1 || diceNum > 10) { //Keeps number of coins at a reasonable level
                    		source.sendMessage(ChatColor.RED + "[Jackpot] ERROR: You can't flip less than " + ChatColor.WHITE + "1 " + ChatColor.RED + "or more than " + ChatColor.WHITE + "10 " + ChatColor.RED + "coins at a time! Setting [coinNum] to [1]");
                    		diceNum = 1;//Resets to default
                    	}
                    	diceValue = flipCoin(diceNum); //Actually flips the coin
                    }
                    boolean hasBroadcasted = false; //Safety to ensure that the message is not broadcasted twice
                    String result = source.getDisplayName() + ChatColor.GOLD + " flipped " + ChatColor.WHITE + diceNum + ChatColor.GOLD + " coin(s) and got: " + ChatColor.RED + diceValue;
                    if(split.length == 2) { //If there's a second argument
                    	if(split[1].toLowerCase().equals("g") && Permissions.has(source, "jackpot.flip.global")) { //if the 2nd arg is 'g' and player has permission
                    		hasBroadcasted = true; //Let the server know that the message has been broadcasted
                    		getServer().broadcastMessage(result); //Broadcast result to the entire server
                    	}
                    }
                    if(!hasBroadcasted) { //If it hasn't been broadcasted yet, do it now
                    	broadcastNear(source, result, defaultDistance);
                    }
                    System.out.println("[Jackpot] " + source.getName() + " flipped " + diceNum + " coin(s) and got: " + diceValue); //Output result to server
            } else { //In case the user does not have permission
            	if(cmdName.equals("cointoss")){ //But were trying to toss a coin anyway
            		source.sendMessage(ChatColor.RED + "You do not have permission to flip coins!"); //Let them knwo that they do not have permission
            	}
            }
                
            if(cmdName.equals("card")){
            	if(split.length == 0) { //No args
            		source.sendMessage(ChatColor.RED + "I'm sorry, but that command requires parameters!");
            	}
            	if(split.length == 1) { //1 argument
            		//Player: SHUFFLE
            		if(split[0].equals("shuffle")) {
            			if(Permissions.has(source, "jackpot.card.shuffle")) {
            				decks.get(source).shuffleDeck();
            				source.sendMessage(ChatColor.GOLD + "You shuffled you deck. All cards have been reset.");
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to shuffle cards!"); }
            		}
            		//Player: DRAW
            		if(split[0].equals("draw")) {
            			if(Permissions.has(source, "jackpot.card.draw")) {
            				String cardDrawn = decks.get(source).drawCard(true);
            				String toSay = source.getDisplayName() + ChatColor.GOLD + " drew a card and got: " + cardDrawn;
            				broadcastNear(source, toSay, defaultDistance);
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to draw cards!"); }
            		}
            	}
            	if(split.length == 2) { //2 args
            		//Player: DEAL
            		if(split[0].equals("deal")) {
            			if(Permissions.has(source, "jackpot.card.deal")) {
            				String lookFor = split[1].toLowerCase();
            				Boolean foundPlayer = false;
            				Player dealTo = source;
            				for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            					String playerName = p.getName();
            		            if(playerName.toLowerCase().equals(lookFor)){
            		            	foundPlayer = true;
            		            	dealTo = p;
            		            	break;
            		            }
            		        }
            				if(foundPlayer) {
            					String dealtCard = decks.get(source).dealCard(dealTo);
            					dealTo.sendMessage(source.getDisplayName() + ChatColor.GOLD + " just dealt you a(n) " + dealtCard);
            					source.sendMessage(ChatColor.GOLD + "You have dealt one card to " + ChatColor.WHITE + dealTo.getDisplayName() + ChatColor.GOLD + ".");
            				} else { source.sendMessage(ChatColor.RED + "Could not find " + ChatColor.WHITE + lookFor + ChatColor.RED + ", are you sure that is the player's name?"); }
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to deal cards!"); }
            		}
            		//See center
            		if(split[0].equals("see")) {
            			if(Permissions.has(source, "jackpot.card.see")) {
            				String lookFor = split[1].toLowerCase();
            				Boolean foundPlayer = false;
            				Player seeMiddle = source;
            				for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            					String playerName = p.getName();
            		            if(playerName.toLowerCase().equals(lookFor)){
            		            	foundPlayer = true;
            		            	seeMiddle = p;
            		            	break;
            		            }
            		        }
            				if(foundPlayer) {
            					ArrayList<String> middleCards = decks.get(seeMiddle).seeCards();
            					String cardsInMiddle = "";
            					for(Integer i = 0; i < middleCards.size(); i++) {
            						if(i < middleCards.size() - 1) {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ", ";
            						} else {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ".";
            						}
            					}
            					String toSay = seeMiddle.getDisplayName() + ChatColor.GOLD + " has the following cards in the center: " + cardsInMiddle;
            					source.sendMessage(toSay);
            				} else { source.sendMessage(ChatColor.RED + "Could not find " + ChatColor.WHITE + lookFor + ChatColor.RED + ", are you sure that is the player's name?"); }
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to see cards!"); }
            		}
            		//See own
            		if(split[0].equals("mine")) {
            			if(Permissions.has(source, "jackpot.card.mine")) {
            				String lookFor = split[1].toLowerCase();
            				Boolean foundPlayer = false;
            				Player seeMiddle = source;
            				for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            					String playerName = p.getName();
            		            if(playerName.toLowerCase().equals(lookFor)){
            		            	foundPlayer = true;
            		            	seeMiddle = p;
            		            	break;
            		            }
            		        }
            				if(foundPlayer) {
            					ArrayList<String> middleCards = decks.get(seeMiddle).getCards(source);
            					String cardsInMiddle = "";
            					for(Integer i = 0; i < middleCards.size(); i++) {
            						if(i < middleCards.size() - 1) {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ", ";
            						} else {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ".";
            						}
            					}
            					String toSay = ChatColor.GOLD + "So far " + seeMiddle.getDisplayName() + " has dealt you: " + cardsInMiddle;
            					source.sendMessage(toSay);
            				} else { source.sendMessage(ChatColor.RED + "Could not find " + ChatColor.WHITE + lookFor + ChatColor.RED + ", are you sure that is the player's name?"); }
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to see your own cards!"); }
            		}
            		//Show own
            		if(split[0].equals("show")) {
            			if(Permissions.has(source, "jackpot.card.show")) {
            				String lookFor = split[1].toLowerCase();
            				Boolean foundPlayer = false;
            				Player seeMiddle = source;
            				for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            					String playerName = p.getName();
            		            if(playerName.toLowerCase().equals(lookFor)){
            		            	foundPlayer = true;
            		            	seeMiddle = p;
            		            	break;
            		            }
            		        }
            				if(foundPlayer) {
            					ArrayList<String> middleCards = decks.get(seeMiddle).getCards(source);
            					String cardsInMiddle = "";
            					for(Integer i = 0; i < middleCards.size(); i++) {
            						if(i < middleCards.size() - 1) {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ", ";
            						} else {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ".";
            						}
            					}
            					String toSay = ChatColor.GOLD + "In " + seeMiddle.getDisplayName() + ChatColor.GOLD + "'s deck, " + ChatColor.WHITE + source.getDisplayName() + ChatColor.GOLD + " has these cards: "+ cardsInMiddle;
            					broadcastNear(source, toSay, defaultDistance);
            				} else { source.sendMessage(ChatColor.RED + "Could not find " + ChatColor.WHITE + lookFor + ChatColor.RED + ", are you sure that is the player's name?"); }
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to show your cards!"); }
            		}
            		//Cards left (ADMIN LVL CMD)
            		if(split[0].equals("left")) {
            			if(Permissions.has(source, "jackpot.card.left")) {
            				String lookFor = split[1].toLowerCase();
            				Boolean foundPlayer = false;
            				Player seeMiddle = source;
            				for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            					String playerName = p.getName();
            		            if(playerName.toLowerCase().equals(lookFor)){
            		            	foundPlayer = true;
            		            	seeMiddle = p;
            		            	break;
            		            }
            		        }
            				if(foundPlayer) {
            					ArrayList<String> middleCards = decks.get(seeMiddle).cardsLeft();
            					String cardsInMiddle = "";
            					for(Integer i = 0; i < middleCards.size(); i++) {
            						if(i < middleCards.size() - 1) {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ", ";
            						} else {
            							cardsInMiddle = cardsInMiddle + middleCards.get(i) + ".";
            						}
            					}
            					source.sendMessage(seeMiddle.getDisplayName() + ChatColor.GOLD + " has the followng cards remaining: " + cardsInMiddle);
            				} else { source.sendMessage(ChatColor.RED + "Could not find " + ChatColor.WHITE + lookFor + ChatColor.RED + ", are you sure that is the player's name?"); }
            			} else { source.sendMessage(ChatColor.RED + "You do not have permission to see remaining cards!"); }
            		}
            	}
            }
            }
            return true;
    }
    
    //All things property related:
    private void loadProperties() {
    	// TODO: Update so that it checks along the way, in case a field is missing
    	Properties prop = new Properties(); //Property handler variable thingy
    	if(FileP.exists()) { //If the file DOES exist, then simply read it
    	try {
        	FileInputStream in = new FileInputStream(FileP); //Loads path for use in reading the file
    		//load a properties file
    		prop.load(in);
    		
    		//Set properties (read from .properties)
    		defaultDistance = Integer.parseInt((prop.getProperty("broadcast-distance")));
    		defaultDiceSize = Integer.parseInt((prop.getProperty("default-dice-size")));
    	} catch (IOException ex) { ex.printStackTrace(); } //Prevent crashing in case of error
    	} else {
    		try {
    			try {
    				boolean success = (new File(FileDir)).mkdirs(); //If it doesn't exist, make the directory before making a file
    			    if (success) { //If the directory was correctly made, let the user know
    			      System.out.println("Directories: " + FileDir + " created");
    			    }

    			    }catch (Exception e){//Catch exception if any
    			      System.err.println("Error: " + e.getMessage());
    			    }
    			FileP.createNewFile(); //Creats the file
    			FileOutputStream out = new FileOutputStream(FileP); //Loads it
        		//Set default properties:
    			prop.setProperty("broadcast-distance", "25");
        		prop.setProperty("default-dice-size", "6");
     
        		//save properties to project root folder
        		prop.store(out, null);
        		
        		System.out.println("[Jackpot] Generating config.properties");
     
        	} catch (IOException ex) { ex.printStackTrace(); } //Prevent crashing
    	}
    }
    
    //Function for broadcasting messages to nearby locations
    private void broadcastNear(Player player, String toBroadcast, Integer distance) {
        for (Player p : getServer().getOnlinePlayers()) { //Runs through every online player, getting distance from [source]
            if (getDistance(p.getLocation(), player.getLocation()) <= distance) { //If they are close enough, then send the message
                p.sendMessage(toBroadcast);
            }
        }
    }
    
    //Function for getting distance between two points:
    private float getDistance(Location p, Location q) {
        return (float) Math.sqrt(Math.pow(p.getBlockX() - q.getBlockX(), 2) + Math.pow(p.getBlockY() - q.getBlockY(), 2) + Math.pow(p.getBlockZ() - q.getBlockZ(), 2));
    }
    
    //Roll a die
    private String rollDie(Integer sides, Integer dice) {
    	Random generator = new Random(); //Init rng
    	String dV = ""; //Init value (to be returned)
    	for(Integer i = 0; i < dice; i++) { //Rolls dice for however many 'dice' were set to roll (english: loops for Interger dice times)
    		Integer r = generator.nextInt(sides) + 1; //Generates a number between 1 and the sides of the dice
    		if(i < dice - 1) { //If it is not yet on the last loop, then separate with a comma
    			dV = dV + r + ", ";
    		} else { //End final loop with a period.
    			dV = dV + r + ".";
    		}
    	}
    	return dV; //Return a string with each number rolled
    }
    
    //Same as rollDie but for coins
    private String flipCoin(Integer numCoins) {
    	Random generator = new Random(); //Init RNG
    	String dV = ""; //Init string (tb returned)
    	for(Integer i = 0; i < numCoins; i++) { //Loops once for each coin that needs to be flipped
    		Integer r = generator.nextInt(2); //Gets a random number between 0 and 1
    		String r2 = "<Heads>"; //Defaults to Heads
    		
    		if(r == 0) { //If r = 0 (which it will 50% of the time) then set result to tails
    			r2 = "<Tails>";
    		}
    		if(i < numCoins - 1) { //Before last loop, seperate values with a comma
    			dV = dV + r2 + ", ";
    		} else { //On last run-through, end with a period
    			dV = dV + r2 + ".";
    		}
    	}
    	return dV; //Return string with all values
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
    
    //GETs()
    public HashMap<Player, Deck> getDeck() {
    	return decks;
    }
    public void addDeck(Player key){
    	Deck newDeck = new Deck(key);
    	decks.put(key, newDeck);
    }
    public void remDeck(Player key){
    	decks.remove(key);
    }
    
    //Sets up permissions hook (for nodes)
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions"); //Finds the Permissions plugin)

        if (this.Permissions == null) {
            if (test != null) {
                this.Permissions = ((Permissions)test).getHandler();
            } else {
                System.out.println("[Jackpot] Permissions not detected, defaulting to Ops-Only"); //Go to Ops only if Permissions is not on the server
            }
        }
    }
}

