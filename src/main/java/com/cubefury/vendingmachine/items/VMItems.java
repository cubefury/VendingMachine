package com.cubefury.vendingmachine.items;

import net.minecraft.item.ItemStack;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;
import com.cubefury.vendingmachine.blocks.MTEVendingUplinkHatch;

import cpw.mods.fml.common.Optional;

public class VMItems {

    public static ItemStack vendingMachine;
    public static ItemStack uplinkHatch;

    public VMItems() {}

    @Optional.Method(modid = "gregtech")
    public static void registerMultis() {
        vendingMachine = new MTEVendingMachine(
            VendingMachine.CONTROLLER_MTE_ID,
            "multimachine.vendingmachine",
            "Vending Machine").getStackForm(1);
        uplinkHatch = new MTEVendingUplinkHatch(
            VendingMachine.ME_UPLINK_MTE_ID,
            "hatch.vendinguplink.me",
            "ME Vending Uplink Hatch").getStackForm(1);
    }
}
