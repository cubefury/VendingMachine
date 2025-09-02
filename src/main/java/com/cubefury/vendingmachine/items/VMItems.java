package com.cubefury.vendingmachine.items;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;

import cpw.mods.fml.common.Optional;

public class VMItems {

    private VMItems() {}

    @Optional.Method(modid = "gregtech")
    public static void registerMultis() {
        new MTEVendingMachine(VendingMachine.CONTROLLER_MTE_ID, "multimachine.vendingmachine", "Vending Machine")
            .getStackForm(1);
    }
}
