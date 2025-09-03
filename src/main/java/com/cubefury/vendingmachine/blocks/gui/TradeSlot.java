package com.cubefury.vendingmachine.blocks.gui;

import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TradeSlot extends PhantomItemSlot {

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        return Result.IGNORE;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        return false;
    }
}
