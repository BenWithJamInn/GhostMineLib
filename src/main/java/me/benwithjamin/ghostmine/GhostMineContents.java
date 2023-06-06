package me.benwithjamin.ghostmine;

import me.benwithjamin.Pair;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/*
 * Project: me.benwithjamin.ghostmine | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 23:15
 */
public class GhostMineContents {
    /**
     * The contents of the mine y, x, z
     */
    private final int[][][] contents;

    public GhostMineContents(int x, int y, int z) {
        this.contents = new int[y][x][z];
    }

    /**
     * Gets a slice of the mine
     *
     * @param y The y level of the slice
     *
     * @return The slice of the mine
     */
    public int[][] getSlice(int y) {
        return this.contents[y];
    }

    /**
     * Sets a single block in the mine
     *
     * @param vector The vector of the block relative to the mine location
     * @param materialData The material data of the block
     */
    public void setBlockRelative(Vector vector, MaterialData materialData) {
        this.getSlice(vector.getBlockY())[vector.getBlockX()][vector.getBlockZ()] = materialDataToint(materialData);
    }

    /**
     * Sets multiple blocks in the mine
     *
     * @param blocks The blocks to set relative to the mine location
     */
    public void setBlocksRelative(HashMap<Vector, MaterialData> blocks) {
        for (Map.Entry<Vector, MaterialData> entry : blocks.entrySet()) {
            MaterialData materialData = entry.getValue();
            Vector vector = entry.getKey();
            this.setBlockRelative(vector, materialData);
        }
    }

    /**
     * Gets a single block in the mine
     *
     * @param vector The vector of the block relative to the mine location
     *
     * @return The material data of the block
     */
    public MaterialData getBlockRelative(Vector vector) {
        return intToMaterialData(this.getSlice(vector.getBlockY())[vector.getBlockX()][vector.getBlockZ()]);
    }

    /**
     * Gets all the blocks in the mine
     *
     * @return The blocks in the mine
     */
    public int[][][] getAllContents() {
        return this.contents;
    }

    /**
     * Converts a vector to a pair of integers (x, z)
     *
     * @param vector The vector to convert
     *
     * @return The pair of integers
     */
    public static Pair<Integer, Integer> vectorToHozPair(Vector vector) {
        return new Pair<>(vector.getBlockX(), vector.getBlockZ());
    }

    /**
     * Converts {@link MaterialData} to a short
     *
     * @param materialData The material data to convert
     * @return The short
     */
    @SuppressWarnings("deprecation")
    public static int materialDataToint(MaterialData materialData) {
        return ((materialData.getItemTypeId() << 8) | materialData.getData());
    }

    /**
     * Converts a short to a {@link MaterialData}
     *
     * @param combined The short to convert
     * @return The material data
     */
    @SuppressWarnings("deprecation")
    public static MaterialData intToMaterialData(int combined) {
        int newType = combined >> 8;
        int newData = combined & 0xFF;
        return new MaterialData(newType, (byte) newData);
    }
}
