package com.cubefury.vendingmachine.trade;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import com.cubefury.vendingmachine.blocks.MTEVendingMachine;

public class TradeRequest {

    public EntityPlayerMP player;
    public UUID playerID;
    public UUID tradeGroup;
    public int tradeGroupOrder;
    MTEVendingMachine target;

    public TradeRequest(EntityPlayerMP player, UUID playerID, UUID tradeGroup, int tradeGroupOrder,
        MTEVendingMachine target) {
        this.player = player;
        this.playerID = playerID;
        this.tradeGroup = tradeGroup;
        this.tradeGroupOrder = tradeGroupOrder;
        this.target = target;
    }
}
