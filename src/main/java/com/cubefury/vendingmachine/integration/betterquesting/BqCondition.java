package com.cubefury.vendingmachine.integration.betterquesting;

import java.util.Objects;
import java.util.UUID;

import com.cubefury.vendingmachine.trade.ICondition;

public class BqCondition implements ICondition {

    private UUID questId;

    public BqCondition(UUID questId) {
        this.questId = questId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BqCondition)) return false;
        return Objects.equals(questId, ((BqCondition) o).questId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(questId);
    }
}
