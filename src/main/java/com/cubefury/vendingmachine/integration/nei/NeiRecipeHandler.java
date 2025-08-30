package com.cubefury.vendingmachine.integration.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.trade.ICondition;
import com.cubefury.vendingmachine.integration.betterquesting.BqAdapter;
import com.cubefury.vendingmachine.integration.betterquesting.BqCondition;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.Translator;

import betterquesting.api2.utils.QuestTranslation;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NeiRecipeHandler extends TemplateRecipeHandler {

    private static final int SLOT_SIZE = 18;
    private static final int GUI_WIDTH = 166;
    private static final int GRID_COUNT = 4;
    private static final int LINE_SPACE = GuiDraw.fontRenderer.FONT_HEIGHT + 1;
    private UUID currentPlayerId;
    private int textColorConditionDefault;
    private int textColorConditionSatisfied;
    private int textColorConditionUnsatisfied;

    private UUID getCurrentPlayerUUID() {
        if (currentPlayerId == null) {
            currentPlayerId = NameCache.INSTANCE.getUUIDFromPlayer(Minecraft.getMinecraft().thePlayer);
        }
        return currentPlayerId;
    }

    private void setTextColors() {
        textColorConditionDefault = Translator.getColor("vendingmachine.gui.neiColor.conditionDefault");
        textColorConditionSatisfied = Translator.getColor("vendingmachine.gui.neiColor.conditionSatisfied");
        textColorConditionUnsatisfied = Translator.getColor("vendingmachine.gui.neiColor.conditionUnsatisfied");
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            setTextColors();
            for (NeiRecipeCache.CacheEntry entry : NeiRecipeCache.recipeCache) {
                this.arecipes.add(new CachedTradeRecipe(entry.trade(), entry.requirements()));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        setTextColors();
        for (NeiRecipeCache.CacheEntry entry : NeiRecipeCache.recipeCache) {
            for (BigItemStack compareTo : entry.trade().toItems) {
                if (matchStack(result, compareTo)) {
                    this.arecipes.add(new CachedTradeRecipe(entry.trade(), entry.requirements()));
                    break;
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        setTextColors();
        for (NeiRecipeCache.CacheEntry entry : NeiRecipeCache.recipeCache) {
            for (BigItemStack compareTo : entry.trade().fromItems) {
                if (matchStack(ingredient, compareTo)) {
                    this.arecipes.add(new CachedTradeRecipe(entry.trade(), entry.requirements()));
                    break;
                }
            }
        }
    }

    public static List<ItemStack> extractStacks(BigItemStack bigStack) {
        if (bigStack.hasOreDict()) {
            List<ItemStack> ret = Arrays.asList(
                bigStack.getOreIngredient()
                    .getMatchingStacks());
            ret.forEach(s -> s.stackSize = bigStack.stackSize);
            return ret;
        } else {
            return Collections.singletonList(translateBigStack(bigStack));
        }
    }

    public static ItemStack translateBigStack(BigItemStack bigStack) {
        ItemStack stack = bigStack.getBaseStack();
        stack.stackSize = bigStack.stackSize;
        return stack;
    }

    private static boolean matchStack(ItemStack compared, BigItemStack bigStackCompareTo) {
        for (ItemStack compareTo : extractStacks(bigStackCompareTo)) {
            if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(compared, compareTo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRecipeName() {
        return VendingMachine.NAME;
    }

    @Override
    public String getGuiTexture() {
        return "vendingmachine:textures/gui/nei.png";
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 0, 0, GUI_WIDTH, 108);
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CachedTradeRecipe recipe = (CachedTradeRecipe) this.arecipes.get(recipeIndex);

        GuiDraw.drawString(Translator.translate("vendingmachine.gui.requirementHeader"), 2, 27, textColorConditionDefault, false);
        for (ICondition condition : recipe.requirements) {
            StringBuilder requirementString = new StringBuilder();
            int color = textColorConditionDefault;
            if (VendingMachine.isBqLoaded && condition instanceof BqCondition) {
                requirementString.append(Translator.translate("vendingmachine.gui.requirement.betterquesting"))
                    .append(": ");
                UUID questId = ((BqCondition) condition).getQuestId();
                // Not sure how long these take to look up
                String questKey = QuestTranslation.buildQuestNameKey(questId);
                String translatedQuestKey = QuestTranslation.translate(questKey);
                if (questKey.equals(translatedQuestKey)) {
                    requirementString.append("Missing Quest");
                } else {
                    requirementString.append(translatedQuestKey);
                }
                color = BqAdapter.INSTANCE.checkPlayerCompletedQuest(currentPlayerId, questId)
                    ? textColorConditionSatisfied
                    : textColorConditionUnsatisfied;
            } else {
                requirementString.append(Translator.translate("vendingmachine.gui.requirement.unknown"));
            }

            List<String> requirementsArray = GuiDraw.fontRenderer
                .listFormattedStringToWidth(requirementString.toString(), GUI_WIDTH);
            int y = 27 + LINE_SPACE;
            for (String line : requirementsArray) {
                GuiDraw.drawString(line, 2, y, color, false);
                y += LINE_SPACE;
            }
        }
    }

    public class CachedTradeRecipe extends CachedRecipe {

        private final List<PositionedStack> inputs = new ArrayList<>();
        private final List<PositionedStack> outputs = new ArrayList<>();
        private final List<ICondition> requirements = new ArrayList<>();

        private CachedTradeRecipe(Trade trade, List<ICondition> requirements) {
            loadInputs(trade);
            loadOutputs(trade);

            this.requirements.addAll(requirements);
        }

        private void loadInputs(Trade trade) {
            int xOffset = 3, y = 7;
            int index = 0;
            for (BigItemStack stack : trade.fromItems) {
                if (index >= GRID_COUNT) {
                    break;
                }
                int x = xOffset + index * SLOT_SIZE;
                inputs.add(new PositionedStack(extractStacks(stack), x, y));
                index++;
            }
        }

        private void loadOutputs(Trade trade) {
            int xOffset = 93, y = 7;
            int index = 0;
            for (BigItemStack stack : trade.toItems) {
                if (index >= GRID_COUNT) {
                    break;
                }
                int x = xOffset + index * SLOT_SIZE;
                outputs.add(new PositionedStack(extractStacks(stack), x, y));
                index++;
            }
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, inputs);
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return outputs;
        }
    }

}
