package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

public class TradeSlot extends PhantomItemSlot {

    private final int x;
    private final int y;
    TradeMainPanel rootPanel;

    public TradeSlot(int x, int y, TradeMainPanel rootPanel) {
        this.x = x;
        this.y = y;
        this.rootPanel = rootPanel;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (rootPanel.shiftHeld) {
            rootPanel.attemptPurchase(x, y);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        return false;
    }
}
