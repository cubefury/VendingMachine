package com.cubefury.vendingmachine.blocks.gui;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cubefury.vendingmachine.trade.CurrencyItem;

public class CoinButton extends ToggleButton {

    private final TradeMainPanel panel;
    private final CurrencyItem.CurrencyType type;

    public CoinButton(TradeMainPanel panel, CurrencyItem.CurrencyType type) {
        super();
        background(IDrawable.EMPTY);
        selectedBackground(IDrawable.EMPTY);

        this.panel = panel;
        this.type = type;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!panel.shiftHeld) {
            return Result.IGNORE;
        }
        switch (mouseButton) {
            case 0:
                next();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            case 1:
                prev();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

}
