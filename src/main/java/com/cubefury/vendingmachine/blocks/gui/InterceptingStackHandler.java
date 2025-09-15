package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;

public class InterceptingStackHandler extends ItemStackHandler {

    private final MTEVendingMachine parent;

    public InterceptingStackHandler(int slots, MTEVendingMachine parent) {
        super(slots);
        this.parent = parent;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (stack != null && isValidIntercept(stack)) {
            interceptStack(stack);
            return;
        }
        super.setStackInSlot(slot, stack);
    }

    private boolean isValidIntercept(ItemStack stack) {
        return stack.getDisplayName()
            .equals("Dirt");
    }

    private void interceptStack(ItemStack stack) {
        VendingMachine.LOG.info("Intercepted stack {} {}", parent.getCurrentUser(), stack);
    }
}
