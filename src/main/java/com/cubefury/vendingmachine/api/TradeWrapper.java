package com.cubefury.vendingmachine.api;

public class TradeWrapper {
    public final Trade trade;
    public final int cooldown;

    public TradeWrapper(Trade trade, int cooldown) {
        this.trade = trade;
        this.cooldown = cooldown;
    }
}
