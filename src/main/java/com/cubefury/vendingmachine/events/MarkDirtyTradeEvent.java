package com.cubefury.vendingmachine.events;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.Event;

// This event is fired whenever a player makes a trade
public class MarkDirtyTradeEvent extends Event {

    private final Collection<UUID> dirtyPlayerIDs;

    public MarkDirtyTradeEvent(UUID player) {
        this.dirtyPlayerIDs = Collections.singleton(player);
    }

    public Collection<UUID> getDirtyPlayerIDs() {
        return dirtyPlayerIDs;
    }
}
