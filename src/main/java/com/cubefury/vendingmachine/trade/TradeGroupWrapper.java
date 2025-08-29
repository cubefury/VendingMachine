package com.cubefury.vendingmachine.trade;

import com.github.bsideup.jabel.Desugar;

@Desugar
// For wrapping tradegroup information for network/vending machine display
public record TradeGroupWrapper(TradeGroup trade, long cooldown, boolean enabled) {}
