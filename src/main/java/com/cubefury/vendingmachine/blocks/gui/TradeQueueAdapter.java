package com.cubefury.vendingmachine.blocks.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;

public class TradeQueueAdapter implements IByteBufAdapter<Queue<TradeItemDisplay>> {

    @Override
    public Queue<TradeItemDisplay> deserialize(PacketBuffer buffer) throws IOException {
        int size = buffer.readVarIntFromBuffer();
        Queue<TradeItemDisplay> queue = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
            if (tag != null) {
                queue.add(TradeItemDisplay.readFromNBT(tag));
            }
        }
        return queue;
    }

    @Override
    public void serialize(PacketBuffer buffer, Queue<TradeItemDisplay> queue) throws IOException {
        buffer.writeVarIntToBuffer(queue.size());
        for (TradeItemDisplay item : queue) {
            buffer.writeNBTTagCompoundToBuffer(item.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public boolean areEqual(@NotNull Queue<TradeItemDisplay> t1, @NotNull Queue<TradeItemDisplay> t2) {
        if (t1.size() != t2.size()) {
            return false;
        }

        Iterator<TradeItemDisplay> it1 = t1.iterator();
        Iterator<TradeItemDisplay> it2 = t2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            TradeItemDisplay d1 = it1.next();
            TradeItemDisplay d2 = it2.next();
            if (!d1.equals(d2)) {
                return false;
            }
        }
        return true;
    }
}
