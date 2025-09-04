package com.cubefury.vendingmachine.blocks.gui;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.TradeGroupWrapper;
import com.cubefury.vendingmachine.trade.TradeManager;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.UUID;


public class TradeMainPanel extends ModularPanel {

    public boolean shiftHeld = false;
    private final MTEVendingMachineGui gui;
    private final PanelSyncManager syncManager;
    private EntityPlayer player = null;
    private int ticksOpen = 0;

    public TradeMainPanel(@NotNull String name, MTEVendingMachineGui gui, PanelSyncManager syncManager) {
        super(name);
        this.gui = gui;
        this.syncManager = syncManager;
    }

    @Override
    public boolean onKeyPressed(char typedChar, int keyCode) {
        // left or right shift
        if (keyCode == 0x2A || keyCode == 0x36) {
            shiftHeld = true;
        }
        return super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        // left or right shift
        if (keyCode == 0x2A || keyCode == 0x36) {
            shiftHeld = false;
        }
        return super.onKeyRelease(typedChar, keyCode);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.player == null && this.syncManager.isInitialised()) {
            this.player = syncManager.getPlayer();
        }
        if (this.ticksOpen % Config.gui_refresh_interval == 0 && player != null && !shiftHeld) {
            VendingMachine.LOG.info("Refreshing Trade Info");
            List<TradeGroupWrapper> trades = TradeManager.INSTANCE.getTrades(NameCache.INSTANCE.getUUIDFromPlayer(syncManager.getPlayer()));

            // TODO: Sort

            // TODO: Update slots
            gui.updateSlots();
        }
        this.ticksOpen += 1;
    }

    public void attemptPurchase(int x, int y) {
        gui.attemptPurchase(x, y);
    }
}
