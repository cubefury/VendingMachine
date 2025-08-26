package com.cubefury.vendingmachine.trade;

import java.util.UUID;

import net.minecraftforge.common.MinecraftForge;

import com.cubefury.vendingmachine.events.MarkDirtyTradeEvent;

public class DirtyTradeMarker {

    public static void markDirty(UUID player) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyTradeEvent(player));
    }
}
