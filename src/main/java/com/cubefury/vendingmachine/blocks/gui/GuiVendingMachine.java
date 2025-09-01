package com.cubefury.vendingmachine.blocks.gui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.util.Translator;

public class GuiVendingMachine {

    public static int uiTextBoxToInventoryGap = 28;
    public static int INPUT_SLOTS_COUNT = 8;

    public final ItemStackHandler inputSlots = new ItemStackHandler(INPUT_SLOTS_COUNT);
    public final SlotGroup inputSlotGroup = new SlotGroup("inputSlots", INPUT_SLOTS_COUNT);

    public ModularPanel getGui(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel("VendingMachine").width(198)
            .heightRel(0.8f)
            .padding(4)
            .bindPlayerInventory();

        syncManager.registerSlotGroup(inputSlotGroup);

        WidgetTheme wt = new WidgetTheme(IDrawable.EMPTY, IDrawable.EMPTY, Color.WHITE.main, 0xFF404040, false);
        return panel.child(
            new Column().sizeRel(1)
                .child(createTitleTextStyle(Translator.translate("tile.vendingmachine.vending_machine.name")))
                .widgetTheme(WidgetThemes.BACKGROUND_TITLE)
                .child(createSearchBarRow())
                .child(createInventory()));
    }

    public IWidget createTitleTextStyle(String title) {
        return new SingleChildWidget<>().coverChildren()
            .topRel(0, -4, 1)
            .leftRel(0, -4, 0)
            .child(
                IKey.str(title)
                    .asWidget()
                    .alignment(Alignment.CENTER)
                    .marginLeft(5)
                    .marginRight(5)
                    .marginTop(5)
                    .marginBottom(1));
        /*
         * String s = String.format("0x%08X",w.getWidgetTheme(ITheme.getDefault()).getColor());
         * VendingMachine.LOG.info("title theme color: {}", s);
         * w.applyTheme(ITheme.getDefault());
         * return w;
         */
    }

    public IWidget createSearchBarRow() {
        return new TextFieldWidget()
                    .left(10).right(10).hintText(Translator.translate("vendingmachine.gui.searchBar.filter"));
    }

    public Flow createInventory() {
        Flow row = new Row().height(18);
        for (int i = 0; i < INPUT_SLOTS_COUNT; i++) {
            row = row.child(new ItemSlot().slot(SyncHandlers.itemSlot(this.inputSlots, i).slotGroup("inputSlots").ignoreMaxStackSize(true)).onUpdateListener(stack -> slotChanged(stack)));
        }
        return row;
    }

    public void slotChanged(ItemSlot slot) {
        VendingMachine.LOG.info("slot changed: {}", slot.getSlot().getStack());
    }

    public IWidget createTradeView() {
        return null;
    }
}
