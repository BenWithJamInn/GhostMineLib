package me.benwithjamin.ghostmine.listeners.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.benwithjamin.ghostmine.GhostMine;
import me.benwithjamin.ghostmine.GhostMineManager;
import me.benwithjamin.ghostmine.GhostMinePacketUtils;
import me.benwithjamin.ghostmine.breakblock.BlockHardness;
import me.benwithjamin.ghostmine.listeners.GhostMineListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.material.MaterialData;

import java.util.HashSet;
import java.util.Set;

/*
 * Project: me.benwithjamin.ghostmine.listeners | Author: BenWithJamIn#4547
 * Created: 02/06/2023 at 14:39
 */
public class BlockDigListener implements GhostMineListener {
    private Set<PacketAdapter> packetAdapters = new HashSet<>();

    @Override
    public void init(GhostMineManager ghostMineManager) {
        // listen to block dig packets
        PacketAdapter digAdapter = new PacketAdapter(
                ghostMineManager.getPlugin(),
                ListenerPriority.NORMAL,
                PacketType.Play.Client.BLOCK_DIG
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                World world = event.getPlayer().getWorld();
                PacketContainer packet = event.getPacket();
                EnumWrappers.PlayerDigType status = packet.getPlayerDigTypes().read(0);
                BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
                Location loc = blockPosition.toLocation(world);
                System.out.println(status);
                if (status != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK && status != EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                    return;
                }
                GhostMine mine = ghostMineManager.getMineAt(loc);
                if (mine == null) {
                    return;
                }
                System.out.println("Cancel block break packet");
                event.setCancelled(true);
                if (status == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                    float time = BlockHardness.getBreakDuration(event.getPlayer(), event.getPlayer().getItemInHand(), mine.getBlock(loc.toVector()).getItemType());
                    System.out.println("time: " + time);
                    if (time != 0) {
                        return;
                    }
                }
                // TODO block break event and cancellation
                System.out.println("broken block in mine at " + loc);
                mine.setBlock(loc.toVector(), new MaterialData(Material.AIR));
            }
        };
        GhostMinePacketUtils.getProtocolManager().addPacketListener(digAdapter);
        this.packetAdapters.add(digAdapter);

        // prevent block update on block interaction
        PacketAdapter blockChangeAdpater = new PacketAdapter(
                ghostMineManager.getPlugin(),
                ListenerPriority.NORMAL,
                PacketType.Play.Server.BLOCK_CHANGE
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                boolean internal = event.getPacket().getMeta("ghostmine").isPresent();
                if (internal) {
                    return;
                }
                World world = event.getPlayer().getWorld();
                PacketContainer packet = event.getPacket();
                BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
                Location loc = blockPosition.toLocation(world);
                GhostMine mine = ghostMineManager.getMineAt(loc);
                if (mine != null) {
                    event.setCancelled(true);
                }
            }
        };
        this.packetAdapters.add(blockChangeAdpater);
        GhostMinePacketUtils.getProtocolManager().addPacketListener(blockChangeAdpater);
    }

    @Override
    public void cleanup() {
        for (PacketAdapter packetAdapter : this.packetAdapters) {
            GhostMinePacketUtils.getProtocolManager().removePacketListener(packetAdapter);
        }
    }
}
