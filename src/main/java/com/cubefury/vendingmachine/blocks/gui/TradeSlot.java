package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cubefury.vendingmachine.trade.TradeCategory;

public class TradeSlot extends PhantomItemSlot {

    private TradeCategory category;
    private int index;
    TradeMainPanel rootPanel;

    public TradeSlot(TradeCategory category, int index, TradeMainPanel rootPanel) {
        this.category = category;
        this.index = index;
        this.rootPanel = rootPanel;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (rootPanel.shiftHeld) {
            rootPanel.attemptPurchase(category, index);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        return false;
    }
}
