package com.cubefury.vendingmachine.gui;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cubefury.vendingmachine.VendingMachine;

public final class GuiTextures {

    public static final UITexture OVERLAY_TRADE_AVAILABLE_HIGHLIGHT = UITexture.builder()
        .location(VendingMachine.MODID, "gui/overlay/trade_available")
        .imageSize(18, 18)
        .canApplyTheme()
        .name("trade_available_highlight")
        .build();

    public static final UITexture TRADE_AVAILABLE_BACKGROUND = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/trade_available")
        .imageSize(18, 18)
        .canApplyTheme()
        .name("trade_available_background")
        .build();
}
