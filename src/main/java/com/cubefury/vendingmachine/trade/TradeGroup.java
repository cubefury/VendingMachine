package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cubefury.vendingmachine.api.TradeManager;

public class TradeGroup {

    private String id = "";
    private final List<Trade> trades = new ArrayList<>();
    private int cooldown = -1;
    private final Set<ICondition> requirementSet = new HashSet<>();
    private final Map<UUID, Set<ICondition>> playerDone = new HashMap<>();

    public TradeGroup(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    public boolean isAvailableUponSatisfied(UUID player, ICondition c) {
        Set<ICondition> tmp = new HashSet<>();
        synchronized (playerDone) {
            if (playerDone.containsKey(player) && playerDone.get(player) == null) {
                tmp.addAll(playerDone.get(player));
            }
        }
        tmp.add(c);
        return tmp.equals(requirementSet);

    }

    public void addSatisfiedCondition(UUID player, ICondition c) {
        synchronized (playerDone) {
            if (!playerDone.containsKey(player) || playerDone.get(player) == null) {
                playerDone.put(player, new HashSet<>());
            }
            playerDone.get(player)
                .add(c);
            if (
                playerDone.get(player)
                    .equals(requirementSet)
            ) {
                TradeManager.INSTANCE.addTradeGroup(player, this);
            }
        }
    }

    public void removeSatisfiedCondition(UUID player, ICondition c) {
        synchronized (playerDone) {
            if (!playerDone.containsKey(player) || playerDone.get(player) == null) {
                return;
            }
            playerDone.get(player)
                .remove(c);
            if (
                !playerDone.get(player)
                    .equals(requirementSet)
            ) {
                TradeManager.INSTANCE.removeTradeGroup(player, this);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TradeGroup) {
            return this.id.equals(((TradeGroup) obj).id);
        }
        return false;
    }
}
