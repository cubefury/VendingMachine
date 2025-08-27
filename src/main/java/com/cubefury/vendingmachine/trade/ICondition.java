package com.cubefury.vendingmachine.trade;

import net.minecraft.nbt.NBTTagCompound;

public interface ICondition {

    void readFromNBT(NBTTagCompound nbt);

    NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
