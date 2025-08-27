package com.cubefury.vendingmachine;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    public boolean isClient() {
        return true;
    }

    @Override
    public EntityPlayer getThePlayer() {
        return FMLClientHandler.instance()
            .getClientPlayerEntity();
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
    }

}
