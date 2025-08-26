package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.TradeManager;
import com.cubefury.vendingmachine.integration.betterquesting.BqAdapter;
import com.cubefury.vendingmachine.integration.betterquesting.BqCondition;

public class TradeGroup {

    private String id = "";
    private final List<Trade> trades = new ArrayList<>();
    private int cooldown = -1;
    private int maxTrades = -1;
    private final Set<ICondition> requirementSet = new HashSet<>();
    private final Map<UUID, Set<ICondition>> playerDone = new HashMap<>();

    public TradeGroup() {}

    public String toString() {
        return this.id;
    }

    public boolean isAvailableUponSatisfied(UUID player, ICondition c) {
        Set<ICondition> tmp = new HashSet<>();
        synchronized (playerDone) {
            if (playerDone.containsKey(player) && playerDone.get(player) == null) {
                tmp.addAll(playerDone.get(player));
            }
        }
        tmp.add(c);
        return tmp.equals(requirementSet);

    }

    public void addSatisfiedCondition(UUID player, ICondition c) {
        synchronized (playerDone) {
            if (!playerDone.containsKey(player) || playerDone.get(player) == null) {
                playerDone.put(player, new HashSet<>());
            }
            playerDone.get(player)
                .add(c);
            if (
                playerDone.get(player)
                    .equals(requirementSet)
            ) {
                TradeManager.INSTANCE.addTradeGroup(player, this);
            }
        }
    }

    public void removeSatisfiedCondition(UUID player, ICondition c) {
        synchronized (playerDone) {
            if (!playerDone.containsKey(player) || playerDone.get(player) == null) {
                return;
            }
            playerDone.get(player)
                .remove(c);
            if (
                !playerDone.get(player)
                    .equals(requirementSet)
            ) {
                TradeManager.INSTANCE.removeTradeGroup(player, this);
            }
        }
    }

    public List<Trade> getTrades() {
        return trades;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TradeGroup) {
            return this.id.equals(((TradeGroup) obj).id);
        }
        return false;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.trades.clear();
        this.requirementSet.clear();

        this.id = nbt.getString("id");
        this.cooldown = nbt.getInteger("cooldown");
        this.maxTrades = nbt.getInteger("maxTrades");
        NBTTagList tradeList = nbt.getTagList("trades", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tradeList.tagCount(); i++) {
            NBTTagCompound trade = tradeList.getCompoundTagAt(i);
            Trade newTrade = new Trade();
            newTrade.readFromNBT(trade);
            this.trades.add(newTrade);
        }
        NBTTagList reqList = nbt.getTagList("requirements", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < reqList.tagCount(); i++) {
            ICondition condition = ConditionParser.getConditionFromNBT(reqList.getCompoundTagAt(i));
            if (condition != null) {
                requirementSet.add(condition);
            }
            if (VendingMachine.isBqLoaded && condition instanceof BqCondition) {
                BqCondition bqc = (BqCondition) condition;
                BqAdapter.INSTANCE.addQuestTrigger(bqc.getQuestId(), this);
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("id", this.id);
        nbt.setInteger("cooldown", this.cooldown);
        nbt.setInteger("maxTrades", this.maxTrades);
        NBTTagList tList = new NBTTagList();
        for (Trade t : trades) {
            tList.appendTag(t.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("trades", tList);
        NBTTagList cList = new NBTTagList();
        for (ICondition ic : requirementSet) {
            cList.appendTag(ic.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("requirements", cList);
        return nbt;
    }
}
