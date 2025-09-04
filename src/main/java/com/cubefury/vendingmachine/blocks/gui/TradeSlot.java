package com.cubefury.vendingmachine.blocks.gui;

import com.cubefury.vendingmachine.VendingMachine;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import gregtech.api.modularui2.GTGuiTextures;

import java.util.List;

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
    public void onMouseStartHover() {
        // VendingMachine.LOG.info("Hovering over {} {}", x, y);
        this.hoverOverlay(GTGuiTextures.OVERLAY_BUTTON_BOUNDING_BOX);
    }

    @Override
    public void onMouseEndHover() {
        this.disableHoverOverlay();
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

    public void onTradeRefresh() {

    }
}
