package com.cubefury.vendingmachine.trade;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record TradeWrapper(Trade trade, long cooldown, boolean enabled) {}
