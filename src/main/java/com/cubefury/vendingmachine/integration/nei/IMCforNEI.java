package com.cubefury.vendingmachine.integration.nei;

import net.minecraft.nbt.NBTTagCompound;

import com.cubefury.vendingmachine.VendingMachine;

import cpw.mods.fml.common.event.FMLInterModComms;

public class IMCforNEI {

    public static void IMCSender() {

        sendCatalyst("vendingmachine", "vendingmachine:vending_machine");
        sendHandler("vendingmachine", "vendingmachine:vending_machine");
    }

    private static void sendHandler(String name, String itemStack) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("handler", name);
        nbt.setString("modName", VendingMachine.NAME);
        nbt.setString("modId", VendingMachine.MODID);
        nbt.setBoolean("modRequired", true);
        nbt.setString("itemName", itemStack);
        nbt.setInteger("handlerHeight", 105);
        nbt.setInteger("handlerWidth", 166);
        nbt.setInteger("maxRecipesPerPage", 3);
        nbt.setInteger("yShift", 0);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", nbt);
    }

    private static void sendCatalyst(String name, String itemStack, int priority) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("handlerID", name);
        nbt.setString("itemName", itemStack);
        nbt.setInteger("priority", priority);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerCatalystInfo", nbt);
    }

    private static void sendCatalyst(String name, String itemStack) {
        sendCatalyst(name, itemStack, 0);
    }
}
