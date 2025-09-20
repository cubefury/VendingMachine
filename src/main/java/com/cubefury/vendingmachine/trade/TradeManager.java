package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;

// This is a cache of available trades, maintained server-side
// so we don't have to recompute what trades are available every time we send it
public class TradeManager {

    public static TradeManager INSTANCE = new TradeManager();

    private final Map<UUID, Set<UUID>> availableTrades = new HashMap<>();

    public final Map<UUID, Map<CurrencyItem.CurrencyType, Integer>> playerCurrency = new HashMap<>();

    // For writeback to file in original format, to prevent data loss
    private final Map<UUID, List<NBTTagCompound>> invalidCurrency = new HashMap<>();

    public boolean hasCurrencyUpdate = false;

    private TradeManager() {}

    public void addTradeGroup(UUID player, UUID tg) {
        synchronized (availableTrades) {
            if (!availableTrades.containsKey(player) || availableTrades.get(player) == null) {
                availableTrades.put(player, new HashSet<>());
            }
            availableTrades.get(player)
                .add(tg);
        }
    }

    public void removeTradeGroup(UUID player, UUID tg) {
        synchronized (availableTrades) {
            if (availableTrades.get(player) != null) {
                availableTrades.get(player)
                    .remove(tg);
            }
        }
    }

    public Set<UUID> getAvailableTrades(@Nonnull UUID player) {
        synchronized (availableTrades) {
            Set<UUID> trades = new HashSet<>();
            if (availableTrades.containsKey(player)) {
                trades.addAll(availableTrades.get(player));
            }
            return trades;
        }
    }

    public void setAvailableTrades(UUID player, Set<UUID> tradeGroups) {
        synchronized (availableTrades) {
            availableTrades.put(player, new HashSet<>());
            availableTrades.get(player)
                .addAll(tradeGroups);
        }
    }

    public void recomputeAvailableTrades(UUID player) {
        synchronized (availableTrades) {
            availableTrades.clear();
            for (Map.Entry<UUID, TradeGroup> entry : TradeDatabase.INSTANCE.getTradeGroups()
                .entrySet()) {
                if (player == null) {
                    for (UUID p : entry.getValue()
                        .getAllUnlockedPlayers()) {
                        availableTrades.computeIfAbsent(p, k -> new HashSet<>());
                        availableTrades.get(p)
                            .add(entry.getKey());
                    }
                } else if (
                    entry.getValue()
                        .isUnlockedPlayer(player)
                ) {
                    availableTrades.computeIfAbsent(player, k -> new HashSet<>());
                    availableTrades.get(player)
                        .add(entry.getKey());
                }
            }
        }
    }

    public List<TradeGroupWrapper> getTrades(UUID player) {
        long currentTimestamp = System.currentTimeMillis();
        synchronized (availableTrades) {
            if (!availableTrades.containsKey(player) || availableTrades.get(player) == null) {
                return new ArrayList<>();
            }
            ArrayList<TradeGroupWrapper> tradeList = new ArrayList<>();
            for (UUID tgId : availableTrades.get(player)) {
                TradeGroup tg = TradeDatabase.INSTANCE.getTradeGroupFromId(tgId);
                long lastTradeTime = tg.getTradeState(player).lastTrade;
                long tradeCount = tg.getTradeState(player).tradeCount;

                long cooldownRemaining;
                if (
                    tg.cooldown != -1 && lastTradeTime != -1 && (currentTimestamp - lastTradeTime) / 1000 < tg.cooldown
                ) {
                    cooldownRemaining = tg.cooldown - (currentTimestamp - lastTradeTime) / 1000;
                } else {
                    cooldownRemaining = -1;
                }

                boolean enabled = tg.maxTrades == -1 || tradeCount < tg.maxTrades;
                tradeList.add(new TradeGroupWrapper(tg, cooldownRemaining, enabled));
            }
            return tradeList;
        }
    }

    public void populateCurrencyFromNBT(NBTTagCompound nbt, UUID player, boolean merge) {
        NBTTagList tagList = nbt.getTagList("playerCurrency", Constants.NBT.TAG_COMPOUND);
        if (!merge) {
            this.playerCurrency.clear();
            this.invalidCurrency.clear();
        }
        this.playerCurrency.computeIfAbsent(player, k -> new HashMap<>());
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound currencyEntry = tagList.getCompoundTagAt(i);
            CurrencyItem.CurrencyType type = CurrencyItem.CurrencyType
                .getTypeFromId(currencyEntry.getString("currency"));
            if (type == null) {
                VendingMachine.LOG.warn("Unknown currency type found: {}", currencyEntry.getString("currency"));
                this.invalidCurrency.computeIfAbsent(player, k -> new ArrayList<>());
                this.invalidCurrency.get(player)
                    .add(currencyEntry);
                continue;
            }
            int amount = currencyEntry.getInteger("amount");
            this.playerCurrency.get(player)
                .computeIfAbsent(type, k -> 0);
            this.playerCurrency.get(player)
                .put(
                    type,
                    amount + (merge ? this.playerCurrency.get(player)
                        .get(type) : 0));
        }
        this.hasCurrencyUpdate = true;
    }

    public NBTTagList writeCurrencyToNBT(UUID player) {
        NBTTagList nbt = new NBTTagList();
        if (this.playerCurrency.get(player) == null) {
            return nbt;
        }
        for (Map.Entry<CurrencyItem.CurrencyType, Integer> entry : this.playerCurrency.get(player)
            .entrySet()) {
            NBTTagCompound currencyEntry = new NBTTagCompound();
            currencyEntry.setString("currency", entry.getKey().id);
            currencyEntry.setInteger("amount", entry.getValue());
            nbt.appendTag(currencyEntry);
        }

        if (this.invalidCurrency.get(player) != null) {
            for (NBTTagCompound tag : this.invalidCurrency.get(player)) {
                nbt.appendTag(tag);
            }
        }
        return nbt;
    }

    public void resetCurrency(UUID playerId, CurrencyItem.CurrencyType type) {
        this.playerCurrency.computeIfAbsent(playerId, k -> new HashMap<>());
        if (type == null) {
            this.playerCurrency.get(playerId)
                .clear();
        } else {
            this.playerCurrency.get(playerId)
                .put(type, 0);
        }
        this.hasCurrencyUpdate = true;
    }

    public void addCurrency(UUID playerId, CurrencyItem mapped) {
        if (mapped != null) {
            this.playerCurrency.computeIfAbsent(playerId, k -> new HashMap<>());
            this.playerCurrency.get(playerId)
                .computeIfAbsent(mapped.type, k -> 0);
            this.playerCurrency.get(playerId)
                .put(
                    mapped.type,
                    this.playerCurrency.get(playerId)
                        .get(mapped.type) + mapped.value);
        }
        this.hasCurrencyUpdate = true;
    }
}
