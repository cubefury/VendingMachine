package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import gregtech.api.modularui2.GTGuiTextures;

public class TradeSlot extends PhantomItemSlot {

    private final int x;
    private final int y;

    public TradeSlot(int x, int y) {
        this.x = x;
        this.y = y;
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
        return Result.IGNORE;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        return false;
    }
}
