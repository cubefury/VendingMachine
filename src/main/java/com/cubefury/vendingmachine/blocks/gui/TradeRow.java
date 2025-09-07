package com.cubefury.vendingmachine.blocks.gui;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

public class TradeRow extends Row {

    public TradeRow() {
        super();
        this.collapseDisabledChild(true)
            .setEnabledIf(
                r -> r.getChildren()
                    .stream()
                    .anyMatch(IWidget::isEnabled));
    }
}
