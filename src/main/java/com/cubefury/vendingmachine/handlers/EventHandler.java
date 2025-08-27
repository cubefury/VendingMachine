package com.cubefury.vendingmachine.handlers;

import com.cubefury.vendingmachine.events.MarkDirtyTradeEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    public static final EventHandler INSTANCE = new EventHandler();

    @SubscribeEvent
    public void onMarkDirtyTrade(MarkDirtyTradeEvent event) {
        SaveLoadHandler.INSTANCE.writeTradeState(event.getDirtyPlayerIDs());
    }

    @SubscribeEvent
    public void onMarkDirtyDb(MarkDirtyDbEvent event) {
        SaveLoadHandler.INSTANCE.writeDatabase();
    }

}
