package com.cubefury.vendingmachine.gui;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cubefury.vendingmachine.VendingMachine;

public final class GuiTextures {

    public static final UITexture OVERLAY_TRADE_AVAILABLE_HIGHLIGHT = UITexture.builder()
        .location(VendingMachine.MODID, "gui/overlay/trade_available")
        .imageSize(47, 25)
        .adaptable(6)
        .name("trade_available_highlight")
        .build();

    public static final UITexture OVERLAY_TRADE_DISABLED = UITexture.builder()
        .location(VendingMachine.MODID, "gui/overlay/trade_disabled")
        .imageSize(47, 25)
        .adaptable(4)
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

    public static final UITexture TRADE_BUTTON_UNPRESSED = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/trade_button_unpressed_color_corrected")
        .imageSize(195, 136)
        .adaptable(4)
        .name("trade_button_unpressed")
        .build();

    public static final UITexture TRADE_BUTTON_PRESSED = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/trade_button_pressed_color_corrected")
        .imageSize(195, 136)
        .adaptable(4)
        .name("trade_button_pressed")
        .build();

    public static final UITexture INPUT_SPRITE = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/input")
        .imageSize(30, 20)
        .name("background_input")
        .build();

    public static final UITexture OUTPUT_SPRITE = UITexture.builder()
        .location(VendingMachine.MODID, "gui/background/output")
        .imageSize(30, 20)
        .name("background_output")
        .build();

    public static final UITexture EJECT_COINS = UITexture.builder()
        .location(VendingMachine.MODID, "gui/overlay/coinEject")
        .imageSize(16, 16)
        .name("coin_eject")
        .build();

    public static final TabTexture TAB_LEFT = TabTexture
        .of(UITexture.fullImage(VendingMachine.MODID, "gui/tabs_left", true), GuiAxis.X, false, 32, 28, 4);
}
