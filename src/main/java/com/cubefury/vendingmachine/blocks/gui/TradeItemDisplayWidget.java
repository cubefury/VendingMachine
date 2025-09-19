package com.cubefury.vendingmachine.blocks.gui;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cubefury.vendingmachine.gui.GuiTextures;

public class TradeItemDisplayWidget extends ItemDisplayWidget implements Interactable {

    private TradeMainPanel rootPanel;
    private boolean pressed = false;
    private IValue<ItemStack> value;

    private TradeItemDisplay display;

    public TradeItemDisplayWidget(TradeItemDisplay display) {
        height(MTEVendingMachineGui.ITEM_HEIGHT);
        width(MTEVendingMachineGui.ITEM_WIDTH);
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
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack item = value.getValue();
        if (!Platform.isStackEmpty(item)) {
            GuiDraw.drawText(" " + this.display.display.stackSize, 4, 9, 1.0f, 0x0, false);
            GuiDraw.drawItem(item, 26, 4, 16, 16, context.getCurrentDrawingZ());
            if (this.display.tradeableNow) {
                GuiDraw.drawOutline(1, 1, 45, 23, 0x883CFF00, 2);
            }
            if (this.display.hasCooldown || !this.display.enabled) {
                GuiDraw.drawRoundedRect(
                    1,
                    1,
                    MTEVendingMachineGui.ITEM_WIDTH - 2,
                    MTEVendingMachineGui.ITEM_HEIGHT - 2,
                    0xBB000000,
                    1,
                    1);
            }
            this.overlay(
                IKey.str(display.hasCooldown ? this.display.cooldownText : "")
                    .style(IKey.WHITE));
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof GenericSyncValue<?>genericSyncValue && genericSyncValue.isOfType(ItemStack.class)) {
            this.value = genericSyncValue.cast();
            return true;
        }
        return false;
    }

    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        this.value = itemSupplier;
        setValue(itemSupplier);
        return this;
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
