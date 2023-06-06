package me.benwithjamin.ghostmine;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import me.benwithjamin.ghostmine.packets.MultiBlockChangeWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Project: me.benwithjamin.ghostmine | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 22:39
 */
public class GhostMinePacketUtils {
    @Getter private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /**
     * Gets the block position from a vector
     *
     * @param vector The vector to get the block position from
     *
     * @return The block position
     */
    public static BlockPosition getBlockPosition(Vector vector) {
        return new BlockPosition(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Gets the wrapped block data from a material data
     *
     * @param materialData The material data to get the wrapped block data from
     *
     * @return The wrapped block data
     */
    @SuppressWarnings("deprecation")
    public static WrappedBlockData getWrappedBlockData(MaterialData materialData) {
        return WrappedBlockData.createData(materialData.getItemType(), materialData.getData());
    }

    /**
     * Gets the chunk coord int pair from a vector
     *
     * @param vector The vector to get the chunk coord int pair from in the players world
     *
     * @return The chunk coord int pair
     */
    public static ChunkCoordIntPair getChunkCoordIntPair(Vector vector) {
        return new ChunkCoordIntPair(vector.getBlockX() >> 4, vector.getBlockZ() >> 4);
    }

    /**
     * Sends a block change to a player
     *
     * @param players The players to send the block change to
     * @param vector The vector of the block to change in the players world
     * @param materialData The material data of the block to change
     */
    public static void sendBlockChange(Set<Player> players, Vector vector, MaterialData materialData) {
        if (materialData == null) {
            materialData = new MaterialData(Material.AIR);
        }
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        packetContainer.getBlockPositionModifier().write(0, getBlockPosition(vector));
        packetContainer.getBlockData().write(0, getWrappedBlockData(materialData));
        packetContainer.setMeta("ghostmine", true);
        for (Player player : players) {
            System.out.println("sending block change to " + player.getName());
            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a multi block change to a player
     *
     * @param players The players to send the multi block change to
     * @param world The world to send the multi block change in
     * @param blocks The blocks to change
     */
    public static void sendMultiBlockChange(Set<Player> players, World world, Map<Vector, MaterialData> blocks) {
        Map<ChunkCoordIntPair, MultiBlockChangeWrapper> packetContainers = new HashMap<>();
        for (Map.Entry<Vector, MaterialData> entry : blocks.entrySet()) {
            ChunkCoordIntPair chunkCoordIntPair = getChunkCoordIntPair(entry.getKey());
            MultiBlockChangeWrapper packetWrapper = packetContainers.computeIfAbsent(chunkCoordIntPair, k -> new MultiBlockChangeWrapper(chunkCoordIntPair));
            MultiBlockChangeInfo info = new MultiBlockChangeInfo(entry.getKey().toLocation(world), getWrappedBlockData(entry.getValue()));
            packetWrapper.addBlockChange(info);
        }
        for (MultiBlockChangeWrapper packetWrapper : packetContainers.values()) {
            for (Player player : players) {
                try {
                    protocolManager.sendServerPacket(player, packetWrapper.build());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends a multi block change to a player
     * Takes raw {@link GhostMineContents} data instead of a map of blocks
     *
     * @param players The players to send the multi block change to
     * @param location The location of the raw blocks (min x, min y, min z)
     * @param rawBlocks The blocks to change
     */
    public static void sendMultiBlockChange(Set<Player> players, Location location, GhostMineContents rawBlocks) {
        Map<ChunkCoordIntPair, MultiBlockChangeWrapper> packetContainers = new HashMap<>();
        int[][][] blockData = rawBlocks.getAllContents();
        for (int y = 0; y < blockData.length; y++) {
            for (int x = 0; x < blockData[y].length; x++) {
                for (int z = 0; z < blockData[y][x].length; z++) {
                    Vector vector = new Vector(x, y, z);
                    MaterialData materialData = rawBlocks.getBlockRelative(vector);
                    ChunkCoordIntPair chunkCoordIntPair = getChunkCoordIntPair(vector);
                    MultiBlockChangeWrapper packetWrapper = packetContainers.computeIfAbsent(chunkCoordIntPair, k -> new MultiBlockChangeWrapper(chunkCoordIntPair));
                    MultiBlockChangeInfo info = new MultiBlockChangeInfo(location.clone().add(vector), getWrappedBlockData(materialData));
                    packetWrapper.addBlockChange(info);
                }
            }
        }
        for (MultiBlockChangeWrapper packetWrapper : packetContainers.values()) {
            for (Player player : players) {
                try {
                    protocolManager.sendServerPacket(player, packetWrapper.build());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
