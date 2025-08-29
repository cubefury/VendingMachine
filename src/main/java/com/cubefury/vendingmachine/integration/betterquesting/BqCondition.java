package com.cubefury.vendingmachine.integration.betterquesting;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;

import com.cubefury.vendingmachine.api.trade.ICondition;

import betterquesting.api.utils.NBTConverter;

public class BqCondition implements ICondition {

    public static String CONDITION_NAME = "betterquesting";
    private UUID questId = null;

    public BqCondition() {}

    public BqCondition(UUID questId) {
        this.questId = questId;
    }

    public UUID getQuestId() {
        return this.questId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BqCondition)) return false;
        return Objects.equals(questId, ((BqCondition) o).questId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(questId);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.questId = NBTConverter.UuidValueType.QUEST.readId(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("name", CONDITION_NAME);
        nbt.setTag("quest", NBTConverter.UuidValueType.QUEST.writeId(this.questId));
        return nbt;
    }

}
