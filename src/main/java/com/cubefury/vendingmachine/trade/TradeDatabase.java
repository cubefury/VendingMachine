package com.cubefury.vendingmachine.trade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.util.NBTConverter;

public class TradeDatabase {

    public static final TradeDatabase INSTANCE = new TradeDatabase();
    public int version = -1;
    private final Map<UUID, TradeGroup> tradeGroups = new HashMap<>();

    private TradeDatabase() {}

    public void clear() {
        tradeGroups.clear();
    }

    public void clearTradeState() {
        tradeGroups.forEach((k, v) -> v.clearTradeState());
    }

    public TradeGroup getTradeGroupFromId(UUID tgId) {
        return tradeGroups.get(tgId);
    }

    public int getTradeGroupCount() {
        return tradeGroups.size();
    }

    public int getTradeCount() {
        return tradeGroups.values()
            .stream()
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
            if (tradeGroups.containsKey(tg.getId())) {
                VendingMachine.LOG.warn("Multiple trade groups with id {} exist in the file!", tg);
                continue;
            }
            tradeGroups.put(tg.getId(), tg);
        }
        if (newIdCount > 0) {
            VendingMachine.LOG.info("Updating {} new trades with UUIDs", newIdCount);
            DirtyDbMarker.markDirty();
        }

    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("version", this.version);
        NBTTagList tgList = new NBTTagList();
        for (TradeGroup tg : tradeGroups.values()) {
            tgList.appendTag(tg.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("tradeGroups", tgList);
        return nbt;
    }

    public void populateTradeStateFromNBT(NBTTagCompound nbt, UUID player) {
        NBTTagList tradeStateList = nbt.getTagList("tradeState", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tradeStateList.tagCount(); i++) {
            NBTTagCompound state = tradeStateList.getCompoundTagAt(i);
            UUID tgId = NBTConverter.UuidValueType.TRADEGROUP.readId(state);
            TradeGroup tg = TradeDatabase.INSTANCE.getTradeGroupFromId(tgId);
            TradeHistory th = new TradeHistory(state.getLong("lastTrade"), state.getInteger("tradeCount"));
            tg.setTradeState(player, th);
        }
    }

    public NBTTagCompound writeTradeStateToNBT(NBTTagCompound nbt, UUID player) {
        NBTTagList tradeStateList = new NBTTagList();
        // This is not very efficient and can take a while to run if there are many trade groups.
        // An alternative is to maintain two copies of this data, one indexing by tradegroup and
        // another indexing by player uuid. Let's do that if it proves to be too slow.
        for (Map.Entry<UUID, TradeGroup> entry : tradeGroups.entrySet()) {
            TradeHistory history = entry.getValue()
                .getTradeState(player);
            if (!history.equals(TradeHistory.DEFAULT)) {
                NBTTagCompound state = new NBTTagCompound();
                NBTConverter.UuidValueType.TRADEGROUP.writeId(entry.getKey(), state);
                state.setLong("lastTrade", history.lastTrade);
                state.setInteger("tradeCount", history.tradeCount);
                tradeStateList.appendTag(state);
            }
        }
        nbt.setTag("tradeState", tradeStateList);
        return nbt;
    }
}
