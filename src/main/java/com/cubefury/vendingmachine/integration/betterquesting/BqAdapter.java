package com.cubefury.vendingmachine.integration.betterquesting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.network.handlers.NetAvailableTradeSync;
import com.cubefury.vendingmachine.network.handlers.NetSatisfiedQuestSync;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;
import com.google.common.collect.ImmutableMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BqAdapter {

    public static final BqAdapter INSTANCE = new BqAdapter();

    private final Map<UUID, Set<TradeGroup>> questUpdateTriggers = new HashMap<>();

    // cache of quests that player has completed, for NEI integration not having
    // to look it up so much
    private final Map<UUID, Set<UUID>> playerSatisfiedCache = new HashMap<>();

    private BqAdapter() {}

    public void resetQuestTriggers(@Nullable UUID quest) {
        if (quest == null) {
            questUpdateTriggers.clear();
        } else {
            questUpdateTriggers.remove(quest);
        }
    }

    public void addQuestTrigger(UUID quest, TradeGroup tg) {
        if (!questUpdateTriggers.containsKey(quest) || questUpdateTriggers.get(quest) == null) {
            questUpdateTriggers.put(quest, new HashSet<>());
        }
        questUpdateTriggers.get(quest)
            .add(tg);
    }

    public Map<UUID, Set<UUID>> getPlayerSatisfiedCache() {
        synchronized (playerSatisfiedCache) {
            return ImmutableMap.copyOf(playerSatisfiedCache);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setPlayerSatisifedCache(Map<UUID, Set<UUID>> newCache) {
        synchronized (playerSatisfiedCache) {
            playerSatisfiedCache.clear();
            playerSatisfiedCache.putAll(newCache);
        }
    }

    public void setQuestFinished(UUID player, UUID quest) {
        if (!questUpdateTriggers.containsKey(quest)) {
            return;
        }
        for (TradeGroup tradeGroup : questUpdateTriggers.get(quest)) {
            tradeGroup.addSatisfiedCondition(player, new BqCondition(quest));
        }
        synchronized (playerSatisfiedCache) {
            playerSatisfiedCache.computeIfAbsent(player, k -> new HashSet<>());
            playerSatisfiedCache.get(player)
                .add(quest);
        }
        syncAvailableTradesFromServer();
    }

    public void setQuestUnfinished(UUID player, UUID quest) {
        for (TradeGroup tradeGroup : questUpdateTriggers.get(quest)) {
            tradeGroup.removeSatisfiedCondition(player, new BqCondition(quest));
            synchronized (playerSatisfiedCache) {
                if (playerSatisfiedCache.get(player) != null) {
                    playerSatisfiedCache.get(player)
                        .remove(quest);
                }
            }
        }
        syncAvailableTradesFromServer();
    }

    public void resetQuests(UUID player) {
        TradeDatabase.INSTANCE.removeAllSatisfiedBqConditions(player);
        synchronized (playerSatisfiedCache) {
            if (player == null) {
                playerSatisfiedCache.clear();
            } else {
                playerSatisfiedCache.remove(player);
            }
        }
        syncAvailableTradesFromServer();
    }

    public void syncAvailableTradesFromServer() {
        // We have to sync these trades even though the trades are only pulled usually during VM GUI opening,
        // cuz someone's teammate might finish the quest
        if (VendingMachine.proxy.isClient()) {
            NetAvailableTradeSync.requestSync();
        }
    }

    public void sendPlayerSatisfiedCache() {
        // Pushes satisfied quest completion data to client,
        NetSatisfiedQuestSync.sendSync();
    }

    public boolean checkPlayerCompletedQuest(UUID player, UUID quest) {
        synchronized (playerSatisfiedCache) {
            VendingMachine.LOG.info(
                "checking {}, {}",
                playerSatisfiedCache.get(player),
                playerSatisfiedCache.get(player) == null ? null
                    : playerSatisfiedCache.get(player)
                        .size());
            return playerSatisfiedCache.get(player) != null && playerSatisfiedCache.get(player)
                .contains(quest);
        }
    }

}
