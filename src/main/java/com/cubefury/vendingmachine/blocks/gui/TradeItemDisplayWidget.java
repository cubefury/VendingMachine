package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cubefury.vendingmachine.gui.GuiTextures;

public class TradeItemDisplayWidget extends ItemDisplayWidget implements Interactable {

    private TradeMainPanel rootPanel;
    private boolean pressed = false;

    private TradeItemDisplay display;

    public TradeItemDisplayWidget(TradeItemDisplay display) {
        size(MTEVendingMachineGui.ITEM_HEIGHT);
        background(
            new DynamicDrawable(() -> pressed ? GuiTextures.TRADE_BUTTON_PRESSED : GuiTextures.TRADE_BUTTON_UNPRESSED));

        this.display = display;
        this.item((ItemStack) null);
    }

    public void setDisplay(TradeItemDisplay display) {
        this.display = display;
        this.item(display == null ? null : display.display);
    }

    public TradeItemDisplay getDisplay() {
        return this.display;
    }

    public @NotNull Interactable.Result onMousePressed(int mouseButton) {
        if (rootPanel.shiftHeld) {
            rootPanel.attemptPurchase(this.display);
            pressed = true;
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public void onMouseEndHover() {
        pressed = false;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        pressed = false;
        return true;
    }

    public void setRootPanel(TradeMainPanel rootPanel) {
        this.rootPanel = rootPanel;
    }
}
