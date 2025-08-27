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
import com.cubefury.vendingmachine.util.NBTConverter;

public class TradeGroup {

    private UUID id = new UUID(0, 0); // placeholder UUID
    private final List<Trade> trades = new ArrayList<>();
    public int cooldown = -1;
    public int maxTrades = -1;
    private final Set<ICondition> requirementSet = new HashSet<>();

    // List of completed conditions for each player
    private final Map<UUID, Set<ICondition>> playerDone = new HashMap<>();

    // List of players with trade history
    private final Map<UUID, TradeHistory> tradeState = new HashMap<>();

    public TradeGroup() {}

    public UUID getId() {
        return this.id;
    }

    public String toString() {
        return this.id.toString();
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
                TradeManager.INSTANCE.addTradeGroup(player, this.id);
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
                TradeManager.INSTANCE.removeTradeGroup(player, this.id);
            }
        }
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void clearTradeState() {
        synchronized (tradeState) {
            tradeState.clear();
        }
    }

    public void resetTradeState(UUID player) {
        if (player != null) {
            synchronized (tradeState) {
                tradeState.remove(player);
            }
        } else {
            clearTradeState();
        }
    }

    public TradeHistory getTradeState(UUID player) {
        synchronized (tradeState) {
            if (!tradeState.containsKey(player) || tradeState.get(player) == null) {
                return new TradeHistory();
            }
            return tradeState.get(player);
        }
    }

    public void setTradeState(UUID player, TradeHistory history) {
        synchronized (tradeState) {
            tradeState.put(player, history);
        }
    }

    public boolean readFromNBT(NBTTagCompound nbt) {
        this.trades.clear();
        this.requirementSet.clear();

        boolean generatedRandomUUID = false;
        if (nbt.hasKey("id")) {
            this.id = NBTConverter.UuidValueType.TRADEGROUP.readId(nbt.getCompoundTag("id"));
        } else {
            this.id = UUID.randomUUID();
            generatedRandomUUID = true;
        }
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
        return generatedRandomUUID;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("id", NBTConverter.UuidValueType.TRADEGROUP.writeId(this.id));
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
