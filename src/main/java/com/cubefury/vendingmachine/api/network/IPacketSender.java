package com.cubefury.vendingmachine.api.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.NetworkRegistry;

public interface IPacketSender {

    // Server to Client
    void sendToPlayers(UnserializedPacket payload, EntityPlayerMP... players);

    void sendToAll(UnserializedPacket payload);

    // Client to Server
    void sendToServer(UnserializedPacket payload);

    // Misc
    void sendToAround(UnserializedPacket payload, NetworkRegistry.TargetPoint point);

    void sendToDimension(UnserializedPacket payload, int dimension);
}
