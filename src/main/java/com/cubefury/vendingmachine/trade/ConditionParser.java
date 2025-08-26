package com.cubefury.vendingmachine.trade;

import net.minecraft.nbt.NBTTagCompound;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.integration.betterquesting.BqCondition;

import cpw.mods.fml.common.Optional;

public class ConditionParser {

    public static ICondition getConditionFromNBT(NBTTagCompound nbt) {
        String condition = nbt.getString("name");

        if (condition.equals(BqCondition.CONDITION_NAME)) {
            if (VendingMachine.isBqLoaded) {
                return getBqConditionFromNBT(nbt);
            } else {
                VendingMachine.LOG.error("Could not initialize betterquesting condition. Is the mod installed?");
            }
        }

        VendingMachine.LOG.error("Could not deserialize condition with name: {}", condition);
        return null;
    }

    public static NBTTagCompound getNBTFromCondition(ICondition condition) {
        return condition.writeToNBT(new NBTTagCompound());
    }

    @Optional.Method(modid = "betterquesting")
    public static BqCondition getBqConditionFromNBT(NBTTagCompound nbt) {
        BqCondition condition = new BqCondition();
        condition.readFromNBT(nbt);
        return condition;
    }

}
