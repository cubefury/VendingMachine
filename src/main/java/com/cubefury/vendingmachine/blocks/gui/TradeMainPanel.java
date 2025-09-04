package com.cubefury.vendingmachine.blocks.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeGroupWrapper;
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.Translator;

public class TradeMainPanel extends ModularPanel {

    public boolean shiftHeld = false;
    private final MTEVendingMachineGui gui;
    private final PanelSyncManager syncManager;
    private final PosGuiData guiData;
    private EntityPlayer player = null;
    private int ticksOpen = 0;

    public TradeMainPanel(@NotNull String name, MTEVendingMachineGui gui, PosGuiData guiData,
        PanelSyncManager syncManager) {
        super(name);
        this.gui = gui;
        this.guiData = guiData;
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
        if (!this.guiData.isClient()) {
            return;
        }
        if (this.player == null && this.syncManager.isInitialised()) {
            this.player = syncManager.getPlayer();
        }
        if (this.ticksOpen % Config.gui_refresh_interval == 0 && player != null && !shiftHeld) {
            VendingMachine.LOG.info("Refreshing Trade Info");
            List<TradeItemDisplay> trades = formatTrades(
                TradeManager.INSTANCE.getTrades(NameCache.INSTANCE.getUUIDFromPlayer(syncManager.getPlayer())));
            gui.updateSlots(trades);
        }
        this.ticksOpen += 1;
    }

    public String convertCooldownText(long cd) {
        if (cd < 60) {
            return cd + Translator.translate("vendingmachine.gui.cooldown_display.second");
        }
        if (cd < 3600) {
            return cd / 60 + Translator.translate("vendingmachine.gui.cooldown_display.minute");
        }
        if (cd < 86400) {
            return cd / 3600 + Translator.translate("vendingmachine.gui.cooldown_display.hour");
        }
        return cd / 86400 + Translator.translate("vendingmachine.gui.cooldown_display.day"); // doom.jpg
    }

    public ItemStack convertToItemStack(BigItemStack stack) {
        ItemStack display = stack.getCombinedStacks()
            .get(0);
        display.stackSize = stack.stackSize;
        return display;
    }

    public List<TradeItemDisplay> formatTrades(List<TradeGroupWrapper> tradeGroups) {
        List<TradeItemDisplay> trades = new ArrayList<>();
        for (TradeGroupWrapper tgw : tradeGroups) {
            List<Trade> tradeList = tgw.trade()
                .getTrades();
            for (int i = 0; i < tradeList.size(); i++) {
                Trade trade = tgw.trade()
                    .getTrades()
                    .get(i);
                BigItemStack displayItem = trade.toItems.get(0);

                trades.add(
                    new TradeItemDisplay(
                        trade.fromItems,
                        trade.toItems,
                        convertToItemStack(displayItem == null ? trade.displayItem : displayItem),
                        tgw.trade()
                            .getId(),
                        i,
                        tgw.trade()
                            .getLabel(),
                        tgw.cooldown(),
                        convertCooldownText(tgw.cooldown()),
                        tgw.cooldown() > 0,
                        tgw.enabled()));
            }
        }
        // Build from bottom of list up
        trades.sort((a, b) -> {
            // null case
            if (a == null || b == null) {
                if (a == b) return 0;
                return b == null ? 1 : -1;
            }
            // disabled trades - will filter down if both are disabled
            if (!a.enabled() || !b.enabled()) {
                if (a.enabled()) {
                    return 1;
                }
                if (b.enabled()) {
                    return -1;
                }
            }
            // trades on cooldown - filter down if equal
            if ((a.hasCooldown() || b.hasCooldown()) && (a.cooldown() != b.cooldown())) {
                return a.cooldown() > b.cooldown() ? 1 : -1;
            }
            // tradegroupID
            if (a.tgID() != b.tgID()) {
                return a.tgID()
                    .compareTo(b.tgID());
            }
            // tradegroup index
            if (a.tradeGroupOrder() == b.tradeGroupOrder()) {
                return 0;
            }
            return a.tradeGroupOrder() > b.tradeGroupOrder() ? 1 : -1;
        });
        return trades;
    }

    public void attemptPurchase(int x, int y) {
        gui.attemptPurchase(x, y);
    }
}
