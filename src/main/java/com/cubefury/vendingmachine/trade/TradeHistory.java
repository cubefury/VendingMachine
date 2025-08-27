package com.cubefury.vendingmachine.trade;

class TradeHistory {
    public long lastTrade = -1;
    public int tradeCount = 0;

    public TradeHistory() {}

    public TradeHistory(long lastTrade, int tradeCount) {
        this.lastTrade = lastTrade;
        this.tradeCount = 0;
    }

    public void executeTrade() {
        lastTrade = System.currentTimeMillis();
        tradeCount += 1;
    }

    public void resetData() {
        lastTrade = -1;
        tradecount = 0;
    }

    public void resetTradeAvailability() {
        lastTrade = -1;
    }

}
