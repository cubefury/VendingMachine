package com.cubefury.vendingmachine.api.trade;

import net.minecraft.nbt.NBTTagCompound;

public interface ICondition {

    void readFromNBT(NBTTagCompound nbt);

    NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
