package com.cubefury.vendingmachine.api;

import com.cubefury.vendingmachine.trade.Trade;
import com.github.bsideup.jabel.Desugar;

@Desugar
public record TradeWrapper(Trade trade, long cooldown, boolean enabled) {}
