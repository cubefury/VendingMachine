package com.cubefury.vendingmachine.integration.betterquesting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.cubefury.vendingmachine.trade.TradeGroup;

public class BqAdapter {

    public static final BqAdapter INSTANCE = new BqAdapter();

    private final Map<UUID, Set<TradeGroup>> questUpdateTriggers = new HashMap<>();

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

    public void setQuestFinished(UUID player, UUID quest) {
        for (TradeGroup tradeGroup : questUpdateTriggers.get(quest)) {
            tradeGroup.addSatisfiedCondition(player, new BqCondition(quest));
        }
    }

}
