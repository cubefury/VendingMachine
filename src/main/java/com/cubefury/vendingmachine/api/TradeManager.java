package com.cubefury.vendingmachine.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;

public class TradeManager {

    public static TradeManager INSTANCE = new TradeManager();

    private final TradeDatabase tradeDB = TradeDatabase.INSTANCE;
    private final Map<UUID, Set<TradeGroup>> availableTrades = new HashMap<>();

    private TradeManager() {}

    public void init() {}

    public void addTradeGroup(UUID player, TradeGroup tg) {
        synchronized (availableTrades) {
            if (!availableTrades.containsKey(player) || availableTrades.get(player) == null) {
                availableTrades.put(player, new HashSet<>());
            }
            availableTrades.get(player)
                .add(tg);
        }
    }

    public void removeTradeGroup(UUID player, TradeGroup tg) {
        synchronized (availableTrades) {
            if (availableTrades.containsKey(player) && availableTrades.get(player) != null) {
                availableTrades.get(player)
                    .remove(tg);
            }
        }
    }

}
