package com.cubefury.vendingmachine.integration.nei;

import static betterquesting.api2.utils.QuestTranslation.buildQuestNameKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.trade.ICondition;
import com.cubefury.vendingmachine.integration.betterquesting.BqCondition;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;
import com.cubefury.vendingmachine.util.Translator;
import com.github.bsideup.jabel.Desugar;

public class NeiRecipeCache {

    public static final List<CacheEntry> recipeCache = new ArrayList<>();

    public static void refreshCache() {
        List<CacheEntry> newList = new ArrayList<>();
        for (Map.Entry<UUID, TradeGroup> entry : TradeDatabase.INSTANCE.getTradeGroups()
            .entrySet()) {
            List<String> requirements = new ArrayList<>();
            for (ICondition condition : entry.getValue()
                .getRequirements()) {
                if (VendingMachine.isBqLoaded && condition instanceof BqCondition) {
                    UUID questId = ((BqCondition) condition).getQuestId();
                    requirements.add(
                        Translator.translate("vendingmachine.gui.requirement.betterquesting")
                            + buildQuestNameKey(questId));
                } else {
                    requirements.add(Translator.translate("vendingmachine.gui.requirement.unknown"));
                    VendingMachine.LOG.warn("Unknown requirement loaded for Vending Machine NEI display");
                }
            }

            for (Trade trade : entry.getValue()
                .getTrades()) {
                newList.add(new CacheEntry(trade, requirements));
            }
        }
        recipeCache.clear();
        recipeCache.addAll(newList);
    }

    @Desugar
    public record CacheEntry(Trade trade, List<String> requirements) {}
}
