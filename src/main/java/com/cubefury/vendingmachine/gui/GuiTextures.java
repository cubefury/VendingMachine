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

    public static final UITexture OVERLAY_TRADE_DISABLED = UITexture.builder()
        .location(VendingMachine.MODID, "gui/overlay/trade_disabled")
        .imageSize(18, 18)
        .canApplyTheme()
        .name("trade_disabled")
        .build();

    public static final UITexture TRADE_AVAILABLE_BACKGROUND = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/trade_available")
        .imageSize(18, 18)
        .canApplyTheme()
        .name("trade_available_background")
        .build();

    public static final UITexture SIDE_PANEL_BACKGROUND = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/panel_side")
        .imageSize(195, 136)
        .adaptable(4)
        .canApplyTheme()
        .name("panel_side_background")
        .build();

    public static final UITexture TEXT_FIELD_BACKGROUND = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/text_field_light_gray")
        .imageSize(61, 12)
        .adaptable(1)
        .canApplyTheme()
        .name("text_field_background")
        .build();
}
