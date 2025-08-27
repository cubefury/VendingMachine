package com.cubefury.vendingmachine.trade;

import net.minecraftforge.common.MinecraftForge;

import com.cubefury.vendingmachine.events.MarkDirtyDbEvent;

public class DirtyDbMarker {

    public static void markDirty() {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyDbEvent());
    }
}
