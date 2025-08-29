package com.cubefury.vendingmachine.network;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.network.IPacketSender;
import com.cubefury.vendingmachine.api.network.UnserializedPacket;
import com.cubefury.vendingmachine.util.ThreadedIO;

import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketSender implements IPacketSender {

    public static final PacketSender INSTANCE = new PacketSender();

    @Override
    public void sendToPlayers(UnserializedPacket payload, EntityPlayerMP... players) {
        payload.payload()
            .setString(
                "ID",
                payload.handler()
                    .toString());

        ThreadedIO.INSTANCE.enqueue(() -> {
            List<NBTTagCompound> fragments = PacketAssembly.INSTANCE.splitPacket(payload.payload());
            for (EntityPlayerMP p : players) {
                for (NBTTagCompound tag : fragments) {
                    VendingMachine.instance.network.sendTo(new SerializedPacket(tag), p);
                }
            }
        });
    }

    @Override
    public void sendToAll(UnserializedPacket payload) {
        payload.payload()
            .setString(
                "ID",
                payload.handler()
                    .toString());

        ThreadedIO.INSTANCE.enqueue(() -> {
            for (NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.payload())) {
                VendingMachine.instance.network.sendToAll(new SerializedPacket(p));
            }
        });
    }

    @Override
    public void sendToServer(UnserializedPacket payload) {
        payload.payload()
            .setString(
                "ID",
                payload.handler()
                    .toString());

        ThreadedIO.INSTANCE.enqueue(() -> {
            for (NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.payload())) {
                VendingMachine.instance.network.sendToServer(new SerializedPacket(p));
            }
        });
    }

    @Override
    public void sendToAround(UnserializedPacket payload, NetworkRegistry.TargetPoint point) {
        payload.payload()
            .setString(
                "ID",
                payload.handler()
                    .toString());

        ThreadedIO.INSTANCE.enqueue(() -> {
            for (NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.payload())) {
                VendingMachine.instance.network.sendToAllAround(new SerializedPacket(p), point);
            }
        });
    }

    @Override
    public void sendToDimension(UnserializedPacket payload, int dimension) {
        payload.payload()
            .setString(
                "ID",
                payload.handler()
                    .toString());

        ThreadedIO.INSTANCE.enqueue(() -> {
            for (NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.payload())) {
                VendingMachine.instance.network.sendToDimension(new SerializedPacket(p), dimension);
            }
        });
    }
}
