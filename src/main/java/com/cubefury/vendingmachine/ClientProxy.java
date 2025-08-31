package com.cubefury.vendingmachine;

import net.minecraft.entity.player.EntityPlayer;

import com.cubefury.vendingmachine.integration.nei.IMCforNEI;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        IMCforNEI.IMCSender();
    }

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
