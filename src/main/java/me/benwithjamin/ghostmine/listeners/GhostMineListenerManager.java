package me.benwithjamin.ghostmine.listeners;

import lombok.Getter;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.listeners.impl.BlockDigListener;
import me.benwithjamin.ghostmine.listeners.impl.BlockPlaceListener;
import me.benwithjamin.ghostmine.listeners.impl.MineSubscriptionListener;
import me.benwithjamin.ghostmine.listeners.impl.RealisticItemListener;
import org.bukkit.event.Listener;

/*
 * Project: org.example.ghostmine | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 23:27
 */
public class GhostMineListenerManager implements Listener {
    /**
     * The max distance a player can be from a mine to listen to it in blocks
     */
    @Getter
    private static final int MAX_LISTEN_DISTANCE = 100;

    public GhostMineListenerManager(GhostMineManager manager) {
        new BlockDigListener().init(manager);
        new MineSubscriptionListener().init(manager);
        new RealisticItemListener().init(manager);
        // TODO: make command/tps switcher
//        new SimpleItemListener().init(manager);
        new BlockPlaceListener().init(manager);
    }
}
