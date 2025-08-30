package com.cubefury.vendingmachine.trade.conditions;

import net.minecraft.nbt.NBTTagCompound;

import com.cubefury.vendingmachine.api.trade.ICondition;

public class UnknownCondition implements ICondition {

    public static String CONDITION_NAME = "unknown";
    private NBTTagCompound data = null;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.data = nbt;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return this.data;
    }
}
