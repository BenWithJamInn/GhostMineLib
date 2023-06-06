package me.benwithjamin.ghostmine.listeners.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.GhostMinePacketUtils;
import me.benwithjamin.ghostmine.listeners.GhostMineListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

/*
 * Project: me.benwithjamin.ghostmine.listeners.impl | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 15:30
 */
public class BlockPlaceListener implements GhostMineListener {
    private PacketAdapter blockPlaceAdapter;

    @Override
    public void init(GhostMineManager ghostMineManager) {
        this.blockPlaceAdapter = new PacketAdapter(
                ghostMineManager.getPlugin(),
                ListenerPriority.NORMAL,
                PacketType.Play.Client.BLOCK_PLACE
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Location loc = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());
                int face = packet.getIntegers().read(0);
                ItemStack heldItem = packet.getItemModifier().read(0);
                GhostMine mine = ghostMineManager.getMineAt(loc);
                if (mine == null) {
                    return;
                }
                Location newBlockLoc = loc.clone();
                if (face == 0) {
                    newBlockLoc.add(0, -1, 0);
                } else if (face == 1) {
                    newBlockLoc.add(0, 1, 0);
                } else if (face == 2) {
                    newBlockLoc.add(0, 0, -1);
                } else if (face == 3) {
                    newBlockLoc.add(0, 0, 1);
                } else if (face == 4) {
                    newBlockLoc.add(-1, 0, 0);
                } else if (face == 5) {
                    newBlockLoc.add(1, 0, 0);
                }
                Block block = newBlockLoc.getBlock();
                block.setType(heldItem.getType());
                block.setData(heldItem.getData().getData());
            }
        };
        GhostMinePacketUtils.getProtocolManager().addPacketListener(this.blockPlaceAdapter);
    }

    @Override
    public void cleanup() {
        GhostMinePacketUtils.getProtocolManager().removePacketListener(this.blockPlaceAdapter);
    }
}
