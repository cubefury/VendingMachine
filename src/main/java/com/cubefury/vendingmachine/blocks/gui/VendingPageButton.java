package com.cubefury.vendingmachine.blocks.gui;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;

public class VendingPageButton extends PageButton {

    private final int index;

    public VendingPageButton(int index, PagedWidget.Controller controller) {
        super(index, controller);

        this.index = index;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MTEVendingMachineGui.lastPage = this.index;
        return super.onMousePressed(mouseButton);
    }
}
