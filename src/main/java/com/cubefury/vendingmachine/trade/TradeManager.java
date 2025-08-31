package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.cubefury.vendingmachine.util.BigItemStack;

// This is a cache of available trades, maintained server-side
// so we don't have to recompute what trades are available every time we send it
public class TradeManager {

    public static TradeManager INSTANCE = new TradeManager();

    private final Map<UUID, Set<UUID>> availableTrades = new HashMap<>();

    private final Map<UUID, List<BigItemStack>> pendingOutputs = new HashMap<>();

    private TradeManager() {}

    public void init() {}

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

    public void addPending(UUID player, List<BigItemStack> pending) {
        if (!pendingOutputs.containsKey(player) || pendingOutputs.get(player) == null) {
            pendingOutputs.put(player, new ArrayList<>());
        }
        pendingOutputs.get(player)
            .addAll(pending);
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
                    cooldownRemaining = (currentTimestamp - lastTradeTime) / 1000;
                } else {
                    cooldownRemaining = -1;
                }

                boolean enabled = tg.maxTrades == -1 || tradeCount < tg.maxTrades;
                tradeList.add(new TradeGroupWrapper(tg, cooldownRemaining, enabled));
            }
            return tradeList;
        }
    }
}
