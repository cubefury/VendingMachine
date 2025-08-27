package com.cubefury.vendingmachine.events;

import cpw.mods.fml.common.eventhandler.Event;

// This event is fired whenever the trade database is modified
// Eg. when trade UUID is autopopulated on loading new trades
public class MarkDirtyDbEvent extends Event {

    public MarkDirtyDbEvent() {}
}
