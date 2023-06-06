package me.benwithjamin;

import lombok.Getter;
import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineContents;
import me.benwithjamin.ghostmine.GhostMineManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {;

    @Override
    public void onEnable() {
        GhostMineManager manager = new GhostMineManager(this);

        int data = GhostMineContents.materialDataToint(new MaterialData(Material.STAINED_CLAY, (byte) 6));
        System.out.println(data);

        GhostMine mine = manager.createGhostMine(new Location(getServer().getWorld("world"), 0, 80, 0), new Vector(30, 30, 30));
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                for (int z = 0; z < 30; z++) {
                    mine.setBlock(new Vector(x, y + 80, z), new MaterialData(Material.STAINED_CLAY, (byte) 6));
                }
            }
        }
    }
}
