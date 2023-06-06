package me.benwithjamin.ghostmine.listeners;

import me.benwithjamin.ghostmine.GhostMineManager;

/*
 * Project: me.benwithjamin.ghostmine.listeners | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 14:38
 */
public interface GhostMineListener {
    void init(GhostMineManager ghostMineManager);
    void cleanup();
}
