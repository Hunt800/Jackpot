package com.Hunt800.Jackpot;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * Jackpot block listener
 * @author Hunt800
 */
public class JackpotBlockListener extends BlockListener {
    private final Jackpot plugin;

    public JackpotBlockListener(final Jackpot plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}
