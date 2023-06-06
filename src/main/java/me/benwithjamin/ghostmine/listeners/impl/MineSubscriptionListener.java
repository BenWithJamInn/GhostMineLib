package me.benwithjamin.ghostmine.listeners.impl;

import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.listeners.GhostMineListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * Project: me.benwithjamin.ghostmine.listeners | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 14:57
 */
public class MineSubscriptionListener implements GhostMineListener, Listener {
    private GhostMineManager ghostMineManager;
    private BukkitRunnable locationRunnable;

    @Override
    public void init(GhostMineManager ghostMineManager) {
        this.ghostMineManager = ghostMineManager;
        ghostMineManager.getPlugin().getServer().getPluginManager().registerEvents(this, ghostMineManager.getPlugin());

        // update mine listeners by player location
        this.locationRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : ghostMineManager.getPlugin().getServer().getOnlinePlayers()) {
                    for (GhostMine mine : ghostMineManager.getMines()) {
                        mine.checkPlayerListener(player, false);
                    }
                }
            }
        };
        this.locationRunnable.runTaskTimer(ghostMineManager.getPlugin(), 0, 20);
    }

    @Override
    public void cleanup() {
        this.locationRunnable.cancel();
        HandlerList.unregisterAll(this);
    }

    // Check if player is in range of mine on teleport
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        for (GhostMine mine : this.ghostMineManager.getMines()) {
            mine.checkPlayerListener(event.getPlayer(), true);
        }
    }

    // remove player from mine listeners on leave
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        for (GhostMine mine : this.ghostMineManager.getMines()) {
            mine.getListeners().remove(event.getPlayer());
        }
    }

    // add player to mine listeners on join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (GhostMine mine : this.ghostMineManager.getMines()) {
            mine.checkPlayerListener(event.getPlayer(), false);
            // runnable to send contents to player after chunk loads
            new BukkitRunnable() {
                @Override
                public void run() {
                    mine.sendContentsToPlayer(event.getPlayer());
                }
            }.runTaskLater(this.ghostMineManager.getPlugin(), 2 * 20); // 2 seconds for now, will experiment
        }
    }
}
