package com.Hunt800.Jackpot;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

/**
 * Handle events for all Player related events
 * @author Hunt800
 */
public class JackpotPlayerListener extends PlayerListener {
    private final Jackpot plugin;

    public JackpotPlayerListener(Jackpot instance) {
        plugin = instance;
    }
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        String name = joiner.getName();
        
        if(plugin.getDeck().containsKey(joiner)){
        	plugin.remDeck(joiner);
        }
        plugin.addDeck(joiner);
        
        System.out.println("[Jackpot] Creating deck for " + name);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
    	Player leaver = event.getPlayer();
    	String name = leaver.getName();
    	
    	if(plugin.getDeck().containsKey(leaver)) {
    		plugin.remDeck(leaver);
    	}
    	System.out.println("[Jackpot] Dumping deck for " + name);
    }
}

