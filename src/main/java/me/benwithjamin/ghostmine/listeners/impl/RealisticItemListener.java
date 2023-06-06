package me.benwithjamin.ghostmine.listeners.impl;

/*
 * Project: me.benwithjamin.ghostmine.listeners | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 14:50
 */

import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.listeners.GhostMineListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Will simulate normal item behaviour, uses more resources
 */
public class RealisticItemListener implements GhostMineListener {
    private final Set<BukkitRunnable> runnables = new HashSet<>();

    @Override
    public void init(GhostMineManager ghostMineManager) {
        // prevent items phasing through blocks
        BukkitRunnable phaseRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : ghostMineManager.getPlugin().getServer().getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() != EntityType.DROPPED_ITEM) {
                            continue;
                        }
                        Location loc = entity.getLocation();
                        GhostMine mine = ghostMineManager.getMineAt(loc);
                        if (mine == null) {
                            continue;
                        }
                        if (!mine.containsLocation(loc)) {
                            continue;
                        }
                        int y = mine.topDownRayCast(loc.getBlockX(), loc.getBlockZ());
                        Location newLoc = entity.getLocation().clone();
                        newLoc.setY(y + 1);
                        entity.teleport(newLoc);
                        entity.setVelocity(new Vector(0, 0, 0));
                    }
                }
            }
        };
        this.runnables.add(phaseRunnable);
        phaseRunnable.runTaskTimer(ghostMineManager.getPlugin(), 0, 5);

        BukkitRunnable pickupRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : ghostMineManager.getPlugin().getServer().getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() != EntityType.DROPPED_ITEM) {
                            continue;
                        }
                        Location loc = entity.getLocation().clone();
                        GhostMine mine = ghostMineManager.getMineAt(loc);
                        if (mine == null) {
                            continue;
                        }
                        loc.subtract(0, 1, 0);
                        for (Player listener : mine.getListeners()) {
                            if (listener.getLocation().distance(loc) > 2) {
                                continue;
                            }
                            // NMS method to pickup item
                            ((CraftItem) entity).getHandle().d(((CraftPlayer) listener).getHandle());
                        }
                    }
                }
            }
        };
        this.runnables.add(pickupRunnable);
        pickupRunnable.runTaskTimer(ghostMineManager.getPlugin(), 0, 20);
    }

    @Override
    public void cleanup() {
        for (BukkitRunnable runnable : runnables) {
            runnable.cancel();
        }
    }
}
