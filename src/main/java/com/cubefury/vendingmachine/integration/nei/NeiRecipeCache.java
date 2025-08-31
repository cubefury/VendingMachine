package com.cubefury.vendingmachine.integration.nei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cubefury.vendingmachine.api.trade.ICondition;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;
import com.github.bsideup.jabel.Desugar;

public class NeiRecipeCache {

    public static final List<CacheEntry> recipeCache = new ArrayList<>();

    public static void refreshCache() {
        List<CacheEntry> newList = new ArrayList<>();
        for (Map.Entry<UUID, TradeGroup> entry : TradeDatabase.INSTANCE.getTradeGroups()
            .entrySet()) {
            List<ICondition> requirements = new ArrayList<>(
                entry.getValue()
                    .getRequirements());

            for (Trade trade : entry.getValue()
                .getTrades()) {
                newList.add(new CacheEntry(trade, requirements));
            }
        }
        recipeCache.clear();
        recipeCache.addAll(newList);
    }

    @Desugar
    public record CacheEntry(Trade trade, List<ICondition> requirements) {}
}
