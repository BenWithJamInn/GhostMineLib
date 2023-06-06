package me.benwithjamin;

import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Iterator;

@SuppressWarnings("all")
@Getter
public class Cuboid implements Iterable<Vector> {

    private int x1, y1, z1, x2, y2, z2;
    private int volume;

    public Cuboid() {} // Gson registration

    public Cuboid(Vector min, Vector max) {
        this.x1 = Math.min(min.getBlockX(), max.getBlockX());
        this.y1 = Math.min(min.getBlockY(), max.getBlockY());
        this.z1 = Math.min(min.getBlockZ(), max.getBlockZ());
        this.x2 = Math.max(min.getBlockX(), max.getBlockX());
        this.y2 = Math.max(min.getBlockY(), max.getBlockY());
        this.z2 = Math.max(min.getBlockZ(), max.getBlockZ());
        this.volume = this.calcVolume();
    }

    public void recalcVolume() {
        this.volume = this.calcVolume();
    }

    private int calcVolume() {
        return (Math.abs(this.getUpperX() - this.getLowerX()) + 1) *
                (Math.abs(this.getUpperY() - this.getLowerY()) + 1) *
                (Math.abs(this.getUpperZ() - this.getLowerZ()) + 1);
    }

    public Vector getMin() {
        return new Vector(this.getLowerX(), this.getLowerY(), this.getLowerZ());
    }

    public Vector getMax() {
        return new Vector(this.getUpperX(), this.getUpperY(), this.getUpperZ());
    }

    public boolean contains(Vector vector) {
        int x = vector.getBlockX();
        int y = vector.getBlockY();
        int z = vector.getBlockZ();
        return x >= this.getMin().getBlockX() && x <= this.getMax().getBlockX()
                && y >= this.getMin().getBlockY() && y <= this.getMax().getBlockY()
                && z >= this.getMin().getBlockZ() && z <= this.getMax().getBlockZ();
    }

    private int getUpperX() {
        return this.x2;
    }

    private int getUpperY() {
        return this.y2;
    }

    private int getUpperZ() {
        return this.z2;
    }

    private int getLowerX() {
        return this.x1;
    }

    private int getLowerY() {
        return this.y1;
    }

    private int getLowerZ() {
        return this.z1;
    }

    public void expand(CuboidDirection dir, int amount) {
        switch (dir) {
            case UNKNOWN: {
                break;
            }
            case NORTH: {
                this.z1 -= amount;
                break;
            }
            case EAST: {
                this.x1 -= amount;
                break;
            }
            case SOUTH: {
                this.z2 += amount;
                break;
            }
            case WEST: {
                this.x2 += amount;
                break;
            }
            case UP: {
                this.y2 += amount;
                break;
            }
            case DOWN: {
                this.y1 -= amount;
                break;
            }
            case DEFAULT: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.UP || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                break;
            }
            case ALL: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid direction " + dir);
            }
        }
    }

    public Vector[] corners() {
        Vector[] res = new Vector[8];
        res[0] = new Vector(this.x1, this.y1, this.z1);
        res[1] = new Vector(this.x1, this.y1, this.z2);
        res[2] = new Vector(this.x1, this.y2, this.z1);
        res[3] = new Vector(this.x1, this.y2, this.z2);
        res[4] = new Vector(this.x2, this.y1, this.z1);
        res[5] = new Vector(this.x2, this.y1, this.z2);
        res[6] = new Vector(this.x2, this.y2, this.z1);
        res[7] = new Vector(this.x2, this.y2, this.z2);
        return res;
    }

    @Override
    @SuppressWarnings("all")
    public CuboidIterator iterator() {
        return new CuboidIterator(this);
    }

    public static class CuboidIterator implements Iterator<Vector> {

        private final int sizeX, sizeY, sizeZ;
        private final Vector min;
        private int x, y, z;
        private int count = 0;
        private int volume;

        private CuboidIterator(Cuboid cuboid) {
            this.min = cuboid.getMin().clone();
            this.sizeX = Math.abs(cuboid.getUpperX() - cuboid.getLowerX()) + 1;
            this.sizeY = Math.abs(cuboid.getUpperY() - cuboid.getLowerY()) + 1;
            this.sizeZ = Math.abs(cuboid.getUpperZ() - cuboid.getLowerZ()) + 1;
            this.x = this.y = this.z = 0;
            this.volume = this.getVolume();
        }

        public int getVolume() {
            return this.sizeX * this.sizeY * this.sizeZ;
        }

        @Override
        public boolean hasNext() {
            return this.count <= this.volume;
        }

        @Override
        public Vector next() {
            if (++this.x >= this.sizeX) {
                this.x = 0;
                if (++this.y >= this.sizeY) {
                    this.y = 0;
                    if (++this.z >= this.sizeZ) {
                        this.z = 0;
                    }
                }
            }
            this.count++;
            return new Vector(this.min.getBlockX() + this.x, this.min.getBlockY() + this.y, this.min.getBlockZ() + this.z);
        }
    }

    public enum CuboidDirection {

        NORTH,
        EAST,
        SOUTH,
        WEST,
        UP,
        DOWN,
        DEFAULT,
        ALL,
        UNKNOWN;

    }
}
