package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.integration.betterquesting.BqAdapter;
import com.cubefury.vendingmachine.integration.nei.NeiRecipeCache;
import com.cubefury.vendingmachine.util.NBTConverter;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TradeDatabase {

    public static final TradeDatabase INSTANCE = new TradeDatabase();
    public int version = -1;
    private final Map<UUID, TradeGroup> tradeGroups = new HashMap<>();
    private final Map<TradeCategory, Set<UUID>> tradeCategories = new HashMap<>();

    private TradeDatabase() {}

    public void clear() {
        tradeGroups.clear();
        tradeCategories.clear();
    }

    public void clearTradeState(UUID player) {
        tradeGroups.forEach((k, v) -> v.clearTradeState(player));
    }

    public TradeGroup getTradeGroupFromId(UUID tgId) {
        return tradeGroups.get(tgId);
    }

    public int getTradeGroupCount() {
        return tradeGroups.size();
    }

    public Map<UUID, TradeGroup> getTradeGroups() {
        return tradeGroups;
    }

    public List<TradeCategory> getTradeCategories() {
        List<TradeCategory> tradeCategoryList = new ArrayList<>(tradeCategories.keySet());
        tradeCategoryList.sort(Comparator.comparing(TradeCategory::ordinal));
        return tradeCategoryList;
    }

    public Set<UUID> getTradeGroupsFromCategory(TradeCategory category) {
        return tradeCategories.get(category);
    }

    public int getTradeCount() {
        return tradeGroups.values()
            .stream()
            .mapToInt(
                tg -> tg.getTrades()
                    .size())
            .sum();
    }

    public void readFromNBT(NBTTagCompound nbt, boolean merge) {
        if (!merge) {
            this.clear();
            if (VendingMachine.isBqLoaded) {
                BqAdapter.INSTANCE.resetQuestTriggers(null);
            }
        }
        int newMetadataCount = 0;
        this.version = nbt.getInteger("version");
        NBTTagList trades = nbt.getTagList("tradeGroups", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < trades.tagCount(); i++) {
            TradeGroup tg = new TradeGroup();
            newMetadataCount += tg.readFromNBT(trades.getCompoundTagAt(i)) ? 1 : 0;
            if (tradeGroups.containsKey(tg.getId())) {
                VendingMachine.LOG.warn("Multiple trade groups with id {} exist in the file!", tg);
                continue;
            }
            tradeCategories.computeIfAbsent(tg.getCategory(), k -> new HashSet<>());
            tradeCategories.get(tg.getCategory())
                .add(tg.getId());

            tradeGroups.put(tg.getId(), tg);
        }
        if (newMetadataCount > 0) {
            VendingMachine.LOG.info("Appended metadata to {} new trades", newMetadataCount);
            DirtyDbMarker.markDirty();
        }
        if (VendingMachine.proxy.isClient()) {
            refreshNeiCache();
        }

        TradeManager.INSTANCE.recomputeAvailableTrades(null);
        VendingMachine.LOG
            .info("Loaded {} trade groups containing {} trade groups.", getTradeGroupCount(), getTradeCount());
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

    public void populateTradeStateFromNBT(NBTTagCompound nbt, UUID player, boolean merge) {
        NBTTagList tradeStateList = nbt.getTagList("tradeState", Constants.NBT.TAG_COMPOUND);
        if (!merge) {
            clearTradeState(player);
        }
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

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "NotEnoughItems")
    public void refreshNeiCache() {
        NeiRecipeCache.refreshCache();
    }

    @Optional.Method(modid = "betterquesting")
    public void removeAllSatisfiedBqConditions(UUID player) {
        for (TradeGroup tg : tradeGroups.values()) {
            tg.removeAllSatisfiedBqConditions(player);
        }
        TradeManager.INSTANCE.recomputeAvailableTrades(player);
    }
}
