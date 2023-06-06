package me.benwithjamin.ghostmine.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;

import java.util.ArrayList;
import java.util.List;

/*
 * Project: org.example.ghostmine.packets | Author: BenWithJamIn#4547
 * Created: 28/05/2023 at 22:55
 */
public class MultiBlockChangeWrapper implements PacketWrapper {
    private final ChunkCoordIntPair chunkCoordIntPair;
    private final List<MultiBlockChangeInfo> multiBlockChangeInfoList = new ArrayList<>();

    public MultiBlockChangeWrapper(ChunkCoordIntPair chunk) {
        this.chunkCoordIntPair = chunk;
    }

    /**
     * Adds a block change to the multi block change packet
     *
     * @param multiBlockChangeInfo The block change to add
     */
    public void addBlockChange(MultiBlockChangeInfo multiBlockChangeInfo) {
        multiBlockChangeInfoList.add(multiBlockChangeInfo);
    }

    @Override
    public PacketContainer build() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        packetContainer.getChunkCoordIntPairs().write(0, chunkCoordIntPair);
        packetContainer.getMultiBlockChangeInfoArrays().write(0, multiBlockChangeInfoList.toArray(new MultiBlockChangeInfo[0]));
        return packetContainer;
    }
}
