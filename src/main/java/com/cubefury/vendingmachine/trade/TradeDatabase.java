package com.cubefury.vendingmachine.trade;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;

public class TradeDatabase {

    public static final TradeDatabase INSTANCE = new TradeDatabase();
    public int version = -1;
    private final Set<TradeGroup> tradeGroups = new HashSet<>();

    private TradeDatabase() {}

    public void clear() {
        tradeGroups.clear();
    }

    public int getTradeGroupCount() {
        return tradeGroups.size();
    }

    public int getTradeCount() {
        return tradeGroups.stream()
            .mapToInt(
                tg -> tg.getTrades()
                    .size())
            .sum();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        int newIdCount = 0;
        this.version = nbt.getInteger("version");
        NBTTagList trades = nbt.getTagList("tradeGroups", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < trades.tagCount(); i++) {
            TradeGroup tg = new TradeGroup();
            newIdCount += tg.readFromNBT(trades.getCompoundTagAt(i)) ? 1 : 0;
            if (tradeGroups.contains(tg)) {
                VendingMachine.LOG.warn("Multiple trade groups with id {} exist in the file!", tg);
            }
            tradeGroups.add(tg);
        }
        if (newIdCount > 0) {
            VendingMachine.LOG.info("Updated {} new trades with UUIDs", newIdCount);
            DirtyDbMarker.markDirty();
        }

    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("version", this.version);
        NBTTagList tgList = new NBTTagList();
        for (TradeGroup tg : tradeGroups) {
            tgList.appendTag(tg.writeToNBT(new NBTTagCompound()));
        }
        return nbt;
    }

}
