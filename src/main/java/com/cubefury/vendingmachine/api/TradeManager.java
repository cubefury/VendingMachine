package com.cubefury.vendingmachine.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;

public class TradeManager {

    public static TradeManager INSTANCE = new TradeManager();

    private final TradeDatabase tradeDB = TradeDatabase.INSTANCE;
    private final Map<UUID, Set<UUID>> availableTrades = new HashMap<>();

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
            if (availableTrades.containsKey(player) && availableTrades.get(player) != null) {
                availableTrades.get(player)
                    .remove(tg);
            }
        }
    }

    public List<TradeWrapper> getTrades(UUID player) {
        long currentTimestamp = System.currentTimeMillis();
        synchronized (availableTrades) {
            if (!availableTrades.containsKey(player) || availableTrades.get(player) == null) {
                return new ArrayList<>();
            }
            ArrayList<TradeWrapper> tradeList = new ArrayList<>();
            for (UUID tgId : availableTrades.get(player)) {
                TradeGroup tg = TradeDatabase.INSTANCE.getTradeGroupFromId(tgId);
                long lastTradeTime = tg.getTradeState(player).lastTrade;
                long tradeCount = tg.getTradeState(player).tradeCount;

                long cooldownRemaining;
                if (
                    tg.cooldown != -1 && lastTradeTime != -1 && (currentTimestamp - lastTradeTime) / 1000 < tg.cooldown
                ) {
                    // surely this type conversion is ok
                    cooldownRemaining = (currentTimestamp - lastTradeTime) / 1000;
                } else {
                    cooldownRemaining = -1;
                }

                boolean enabled = tg.maxTrades == -1 || tradeCount < tg.maxTrades;
                for (Trade trade : tg.getTrades()) {
                    tradeList.add(new TradeWrapper(trade, cooldownRemaining, enabled));
                }
            }
            return tradeList;
        }
    }
}
