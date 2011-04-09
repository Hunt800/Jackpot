package com.Hunt800.Jackpot;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Hunt800
 */
public class JackpotPlayerListener extends PlayerListener {
    private final Jackpot plugin;

    public JackpotPlayerListener(Jackpot instance) {
        plugin = instance;
    }

    //Insert Player related code here
}

