package com.cubefury.vendingmachine.blocks.gui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;

public final class WidgetThemes {

    public static String BACKGROUND_TITLE = "vendingmachine:backgroundTitle";
    public static String TEXT_TITLE = "vendingmachine:textTitle";
    public static String OVERLAY_ITEM_SLOT_TRADEABLE = "vendingmachine:slotTradeable";

    public static void register() {
        IThemeApi themeApi = IThemeApi.get();

        themeApi.registerWidgetTheme(
            TEXT_TITLE,
            new WidgetTheme(IDrawable.EMPTY, IDrawable.EMPTY, Color.WHITE.main, 0x404040, false),
            (WidgetTheme::new));

        registerThemedTexture(BACKGROUND_TITLE);
        registerThemedItemSlot(OVERLAY_ITEM_SLOT_TRADEABLE);
    }

    public static void registerThemedTexture(String textureThemeId) {
        IThemeApi themeApi = IThemeApi.get();
        themeApi.registerWidgetTheme(
            textureThemeId,
            new WidgetTheme(IDrawable.EMPTY, IDrawable.EMPTY, Color.WHITE.main, 0xFF404040, false),
            WidgetTheme::new);
    }

    private static void registerThemedItemSlot(String textureThemeId) {
        IThemeApi themeApi = IThemeApi.get();
        themeApi.registerWidgetTheme(
            textureThemeId,
            new WidgetSlotTheme(GuiTextures.SLOT_ITEM, Color.withAlpha(Color.WHITE.main, 0x60)),
            WidgetSlotTheme::new);
    }

}
