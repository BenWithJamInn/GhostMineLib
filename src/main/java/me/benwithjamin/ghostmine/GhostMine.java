package me.benwithjamin.ghostmine;

import lombok.Getter;
import me.benwithjamin.ghostmine.listeners.GhostMineListenerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import me.benwithjamin.Cuboid;

import java.util.*;

/*
 * Project: me.benwithjamin.ghostmine | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 22:14
 */
@Getter
public class GhostMine {
    /** contents of the mine */
    private GhostMineContents contents;
    /** The location of the mine - min vector */
    private Location location;
    /** The dimensions of the mine */
    private Cuboid cuboid;
    /** The players listening to the mine */
    private final Set<Player> listeners = new HashSet<>();
    /** The current dimensions of the mine */
    private Vector dimensions;

    /**
     * @param location  The location of the mine (min vector)
     * @param dimensions The dimensions of the mine
     */
    public GhostMine(Location location, Vector dimensions) {
        this.contents = new GhostMineContents(dimensions.getBlockX(), dimensions.getBlockY(), dimensions.getBlockZ());
        this.location = location;
        this.cuboid = new Cuboid(location.toVector(), location.toVector().add(dimensions.subtract(new Vector(1, 1, 1))));
        this.dimensions = dimensions;
    }

    /**
     * Sets a single block in the mine
     *
     * @param vector       The vector of the block in the mines world
     * @param materialData The material data of the block
     * @param ignoredPlayers The players to ignore when sending the block
     */
    public void setBlock(Vector vector, MaterialData materialData, Player... ignoredPlayers) {
        Vector modVec = vector.clone().subtract(this.location.toVector());
        this.contents.setBlockRelative(modVec, materialData);
        // update listeners
        this.sendBlockToListeners(vector, materialData, ignoredPlayers);
    }

    /**
     * Sets multiple blocks in the mine
     *
     * @param blocks The blocks to set in the mines world
     */
    public void setBlocks(HashMap<Vector, MaterialData> blocks) {
        for (Map.Entry<Vector, MaterialData> entry : blocks.entrySet()) {
            MaterialData materialData = entry.getValue();
            Vector vector = entry.getKey();
            this.setBlock(vector, materialData);
        }
        // update listeners
        this.sendBlocksToListeners(blocks);
    }

    /**
     * Gets a single block in the mine
     *
     * @param vector The vector of the block in the mines world
     * @return The material data of the block
     */
    public MaterialData getBlock(Vector vector) {
        return this.contents.getBlockRelative(vector.subtract(this.location.toVector()));
    }

    /**
     * Checks if player is eligible to listen to the mine
     * If eligible, adds them to the listeners
     * If not eligible, removes them from the listeners
     *
     * @param player              The player to check
     * @param forceUploadContents Whether to force upload the contents of the mine to the player
     * @return Whether the player is eligible to listen to the mine
     */
    public boolean checkPlayerListener(Player player, boolean forceUploadContents) {
        if (player.getWorld() != this.location.getWorld()) {
            this.listeners.remove(player);
            return false;
        }
        if (player.getLocation().distance(this.location) > GhostMineListenerManager.getMAX_LISTEN_DISTANCE()) {
            this.listeners.remove(player);
            return false;
        }
        if (forceUploadContents || !this.listeners.contains(player)) {
            this.sendContentsToPlayer(player);
        }
        this.listeners.add(player);
        return true;
    }

    /**
     * Sends a block change to all listeners
     *
     * @param vector The vector of the block in the mines world
     * @param block  The material data of the block
     * @param ignoredPlayers The players to not send updates to
     */
    public void sendBlockToListeners(Vector vector, MaterialData block, Player... ignoredPlayers) {
        if (this.listeners.isEmpty()) {
            return;
        }
        Set<Player> targets = new HashSet<>(this.listeners);
        Arrays.asList(ignoredPlayers).forEach(targets::remove);
        if (targets.isEmpty()) {
            return;
        }
        GhostMinePacketUtils.sendBlockChange(targets, vector, block);
    }

    /**
     * Sends multiple block changes to all listeners
     *
     * @param blocks The blocks to send in the mines world
     */
    public void sendBlocksToListeners(HashMap<Vector, MaterialData> blocks) {
        if (this.listeners.isEmpty()) {
            return;
        }
        Map<Vector, MaterialData> globalBlocks = new HashMap<>();
        for (Map.Entry<Vector, MaterialData> entry : blocks.entrySet()) {
            Vector vector = entry.getKey();
            MaterialData materialData = entry.getValue();
            globalBlocks.put(this.relativeToGlobal(vector), materialData);
        }
        GhostMinePacketUtils.sendMultiBlockChange(this.listeners, this.location.getWorld(), globalBlocks);
    }

    /**
     * Sends the contents of the mine to a player
     *
     * @param player The player to send the contents to
     */
    public void sendContentsToPlayer(Player player) {
        System.out.println("Sending contents to player");
        GhostMinePacketUtils.sendMultiBlockChange(new HashSet<Player>() {{
            add(player);
        }}, this.location, this.contents);
    }

    /**
     * Translates a relative vector to a global vector
     *
     * @param relative The relative vector
     * @return The global vector
     */
    public Vector relativeToGlobal(Vector relative) {
        return relative.clone().add(this.location.toVector());
    }

    /**
     * Checks if a location is within the mine
     *
     * @param location The location to check
     *
     * @return Whether the location is within the mine
     */
    public boolean containsLocation(Location location) {
        return this.cuboid.contains(location.toVector()) && location.getWorld() == this.location.getWorld();
    }

    /**
     * Returns the block y coord at the top of the mine at the given x and z
     *
     * @param x The x coord
     * @param z The z coord
     *
     * @return The y coord of the top of the mine block
     */
    public int topDownRayCast(int x, int z) {
        int highestCoord = 0;
        x -= this.location.getBlockX();
        z -= this.location.getBlockZ();
        int[][][] contents = this.contents.getAllContents();
        for (int y = 0; y < contents.length; y++) {
            MaterialData materialData = this.contents.getBlockRelative(new Vector(x, y, z));
            if (materialData == null || materialData.getItemType() == Material.AIR) {
                continue;
            }
            highestCoord = y;
        }
        return (int) (highestCoord + this.location.getY());
    }

    /**
     * This will reposition the mine to a new location
     * This WILL NOT update the listeners of the mine and is up to the caller to do so
     *
     * @param newLoc The new location of the mine
     */
    public void relocate(Location newLoc) {
        this.location = newLoc;
        this.contents = new GhostMineContents(this.dimensions.getBlockX(), this.dimensions.getBlockY(), this.dimensions.getBlockZ());
    }

    /**
     * This will reposition the mine to a new location and resize it
     * This WILL NOT update the listeners of the mine and is up to the caller to do so
     *
     * @param newLoc The new location of the mine
     * @param dimensions The new dimensions of the mine
     */
    public void relocate(Location newLoc, Vector dimensions) {
        this.location = newLoc;
        this.resize(dimensions);
    }

    /**
     * This will resize the mine to a new size
     * This WILL NOT update the listeners of the mine and is up to the caller to do so
     *
     * @param dimensions The new dimensions of the mine
     */
    public void resize(Vector dimensions) {
        this.dimensions = dimensions;
        this.cuboid = new Cuboid(this.location.toVector(), this.location.toVector().add(this.dimensions));
        this.contents = new GhostMineContents(this.dimensions.getBlockX(), this.dimensions.getBlockY(), this.dimensions.getBlockZ());
    }
}
