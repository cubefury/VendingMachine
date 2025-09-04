package com.cubefury.vendingmachine.blocks.gui;

import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;

import com.cubefury.vendingmachine.util.BigItemStack;
import com.github.bsideup.jabel.Desugar;

@Desugar
public record TradeItemDisplay(List<BigItemStack> fromItems, List<BigItemStack> toItems, ItemStack display, UUID tgID, // TradeGroup
                                                                                                                       // UUID
    int tradeGroupOrder, // ordering within tradegroup
    String label, // additional text for tooltip
    long cooldown, String cooldownText, boolean hasCooldown, boolean enabled) {}
