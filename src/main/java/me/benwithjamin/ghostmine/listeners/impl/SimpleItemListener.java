package me.benwithjamin.ghostmine.listeners.impl;

import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.listeners.GhostMineListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/*
 * Project: me.benwithjamin.ghostmine.listeners | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 15:06
 */
public class SimpleItemListener implements GhostMineListener {
    private BukkitRunnable itemRunnable;

    @Override
    public void init(GhostMineManager ghostMineManager) {
        this.itemRunnable = new BukkitRunnable() {
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
                        Location teleportLoc = mine.getLocation().clone();
                        // teleport to top of mine
                        teleportLoc.setY(teleportLoc.getY() + mine.getDimensions().getBlockY() + 1);
                        // find what edges the item is closest to
                        Vector dimensionMidpoint = mine.getDimensions().clone().multiply(0.5);
                        Vector entityRelative = loc.toVector().subtract(mine.getLocation().toVector());
                        if (entityRelative.getBlockX() > dimensionMidpoint.getBlockX()) {
                            teleportLoc.setX(teleportLoc.getX() + mine.getDimensions().getBlockX());
                        } else {
                            teleportLoc.setX(teleportLoc.getX() - 1);
                        }
                        if (entityRelative.getBlockZ() > dimensionMidpoint.getBlockZ()) {
                            teleportLoc.setZ(teleportLoc.getZ() + mine.getDimensions().getBlockZ());
                        } else {
                            teleportLoc.setZ(teleportLoc.getZ() - 1);
                        }
                        // teleport item to top of mine
                        entity.teleport(teleportLoc);
                    }
                }
            }
        };
        this.itemRunnable.runTaskLater(ghostMineManager.getPlugin(), 5);
    }

    @Override
    public void cleanup() {
        this.itemRunnable.cancel();
    }
}
