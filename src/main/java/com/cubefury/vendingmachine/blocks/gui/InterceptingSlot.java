package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cubefury.vendingmachine.VendingMachine;

public class InterceptingSlot extends ModularSlot {

    public InterceptingSlot(ItemStackHandler inputItems, int index) {
        super(inputItems, index);
    }

    // intercept item on both ends, but only do the post-intercept actions on server side
    public boolean intercept(ItemStack newItem, boolean client, EntityPlayer player) {
        if (
            newItem != null && newItem.getDisplayName()
                .equals("Dirt")) {
            VendingMachine.LOG.info("intercept {} {}", newItem, player);
            this.putStack(null);
            return true;
        }
        return false;
    }

}
