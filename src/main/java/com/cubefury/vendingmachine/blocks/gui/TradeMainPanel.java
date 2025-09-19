package com.cubefury.vendingmachine.blocks.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.blocks.MTEVendingMachine;
import com.cubefury.vendingmachine.network.handlers.NetResetVMUser;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeCategory;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;
import com.cubefury.vendingmachine.trade.TradeGroupWrapper;
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.Translator;

import codechicken.nei.SearchField;
import codechicken.nei.api.ItemFilter;

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

    public void updateGui() {
        boolean test = true;
        if (test) {
            List<TradeGroupWrapper> testTGW = new ArrayList<>();
            for (Map.Entry<UUID, TradeGroup> entry : TradeDatabase.INSTANCE.getTradeGroups()
                .entrySet()) {
                testTGW.add(new TradeGroupWrapper(entry.getValue(), -1, true));
            }
            Map<TradeCategory, List<TradeItemDisplay>> trades = formatTrades(testTGW);
            gui.updateSlots(trades);
        } else {
            Map<TradeCategory, List<TradeItemDisplay>> trades = formatTrades(
                TradeManager.INSTANCE.getTrades(NameCache.INSTANCE.getUUIDFromPlayer(syncManager.getPlayer())));
            gui.updateSlots(trades);
        }
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
        if (TradeManager.INSTANCE.hasCurrencyUpdate) {
            MTEVendingMachineGui.setForceRefresh();
        }
        if (
            MTEVendingMachineGui.forceRefresh
                || (this.ticksOpen % Config.gui_refresh_interval == 0 && player != null && !shiftHeld)
        ) {
            updateGui();
            MTEVendingMachineGui.resetForceRefresh();
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

    public boolean checkItemsSatisfied(List<BigItemStack> trade, Map<BigItemStack, Integer> availableItems) {
        for (BigItemStack bis : trade) {
            BigItemStack base = bis.copy();
            base.stackSize = 1; // shouldn't need this, but just in case
            if (availableItems.get(base) == null || availableItems.get(base) < bis.stackSize) {
                return false;
            }
        }
        return true;
    }

    public Map<BigItemStack, Integer> getAvailableItems() {
        Map<BigItemStack, Integer> items = new HashMap<>();
        for (int i = 0; i < MTEVendingMachine.INPUT_SLOTS; i++) {
            ItemStack stack = this.gui.getBase().inputItems.getStackInSlot(i);
            if (stack != null) {
                BigItemStack tmp = new BigItemStack(stack);
                tmp.stackSize = 1;
                items.putIfAbsent(tmp, 0);
                items.replace(tmp, items.get(tmp) + stack.stackSize);
            }
        }
        return items;
    }

    public Map<TradeCategory, List<TradeItemDisplay>> formatTrades(List<TradeGroupWrapper> tradeGroups) {

        Map<BigItemStack, Integer> availableItems = this.guiData.isClient() && this.gui.getBase() != null
            ? getAvailableItems()
            : new HashMap<>();

        Map<TradeCategory, List<TradeItemDisplay>> trades = new HashMap<>();
        trades.put(TradeCategory.ALL, new ArrayList<>());

        for (TradeGroupWrapper tgw : tradeGroups) {
            List<Trade> tradeList = tgw.trade()
                .getTrades();
            TradeCategory category = tgw.trade()
                .getCategory();
            trades.putIfAbsent(category, new ArrayList<>());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade trade = tgw.trade()
                    .getTrades()
                    .get(i);
                BigItemStack displayItem = trade.toItems.get(0);
                TradeItemDisplay tid = new TradeItemDisplay(
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
                    tgw.enabled(),
                    checkItemsSatisfied(trade.fromItems, availableItems));

                trades.get(category)
                    .add(tid);
                trades.get(TradeCategory.ALL)
                    .add(tid);
            }
        }

        String searchString = gui.getSearchBarText();
        ItemFilter filter = SearchField.getFilter(searchString);

        for (TradeCategory category : trades.keySet()) {
            List<TradeItemDisplay> filteredTrades = trades.get(category);
            filteredTrades = filteredTrades.stream()
                .filter(tid -> tid.satisfiesSearch(filter, searchString.toLowerCase()))
                .collect(Collectors.toList());
            filteredTrades.sort((a, b) -> {
                // null case
                if (a == null && b == null) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                if (a.display.getItem() == null && b.display.getItem() == null) return 0;
                if (a.display.getItem() == null) return 1;
                if (b.display.getItem() == null) return -1;

                // enabled or has cooldown
                int rankA = getRank(a);
                int rankB = getRank(b);

                if (rankA != rankB) {
                    return Integer.compare(rankA, rankB);
                }

                // cooldown time
                int cooldownCmp = Long.compare(b.cooldown, a.cooldown);
                if (cooldownCmp != 0) return cooldownCmp;

                // display item ordering
                int idCmp = Integer
                    .compare(Item.getIdFromItem(a.display.getItem()), Item.getIdFromItem(b.display.getItem()));
                if (idCmp != 0) return idCmp;
                int dmgCmp = Integer.compare(a.display.getItemDamage(), b.display.getItemDamage());
                if (dmgCmp != 0) return dmgCmp;

                // sort by tradegroup Order
                return Integer.compare(a.tradeGroupOrder, b.tradeGroupOrder);

            });
            trades.replace(category, filteredTrades);
        }
        return trades;
    }

    private static int getRank(TradeItemDisplay t) {
        if (!t.enabled) {
            return 5;
        }
        if (t.tradeableNow) {
            return t.hasCooldown ? 2 : 1;
        }
        return t.hasCooldown ? 4 : 3;
    }

    public void attemptPurchase(TradeItemDisplay display) {
        gui.attemptPurchase(display);
    }

    @Override
    public void dispose() {
        this.gui.getBase()
            .resetCurrentUser(this.player);
        // We have to sync reset use manually since dispose() is only run client-side
        NetResetVMUser.sendReset(this.gui.getBase());
        super.dispose();
    }
}
