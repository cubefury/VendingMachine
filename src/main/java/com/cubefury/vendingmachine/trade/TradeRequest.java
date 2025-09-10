package com.cubefury.vendingmachine.trade;

import java.util.UUID;

import com.cubefury.vendingmachine.blocks.MTEVendingMachine;

public class TradeRequest {

    public UUID player;
    public UUID tradeGroup;
    public int tradeGroupOrder;
    MTEVendingMachine target;

    public TradeRequest(UUID player, UUID tradeGroup, int tradeGroupOrder, MTEVendingMachine target) {
        this.player = player;
        this.tradeGroup = tradeGroup;
        this.tradeGroupOrder = tradeGroupOrder;
        this.target = target;
    }
}
