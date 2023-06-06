package me.benwithjamin.ghostmine;

import lombok.Getter;
import me.benwithjamin.ghostmine.listeners.GhostMineListenerManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/*
 * Project: me.benwithjamin.ghostmine | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 22:14
 */
@Getter
public class GhostMineManager {
    private final Set<GhostMine> mines = new HashSet<>();
    private final JavaPlugin plugin;

    public GhostMineManager(JavaPlugin plugin) {
        this.plugin = plugin;
        new GhostMineListenerManager(this);
    }

    /**
     * Creates a new ghost mine
     *
     * @param location The location of the mine
     *
     * @return The ghost mine
     */
    public GhostMine createGhostMine(Location location, Vector dimensions) {
        GhostMine mine = new GhostMine(location, dimensions);
        mines.add(mine);
        return mine;
    }

    /**
     * Returns the mine that contains the given location
     *
     * @param location The location to check
     *
     * @return The mine that contains the location or null if no mine is found
     */
    public GhostMine getMineAt(Location location) {
        for (GhostMine mine : this.mines) {
            if (mine.getLocation().getWorld() != location.getWorld()) {
                continue;
            }
            if (mine.getCuboid().contains(location.toVector())) {
                return mine;
            }
        }
        return null;
    }
}
