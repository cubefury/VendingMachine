package com.cubefury.vendingmachine;

import net.minecraft.block.Block;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cubefury.vendingmachine.blocks.BlockVendingMachine;
import com.cubefury.vendingmachine.blocks.TileVendingMachine;
import com.cubefury.vendingmachine.util.ItemPlaceholder;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.Type;

@Mod(
    modid = VendingMachine.MODID,
    version = Tags.VERSION,
    name = "VendingMachine",
    acceptedMinecraftVersions = "[1.7.10]")
public class VendingMachine {

    public static final String MODID = "vendingmachine";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static VendingMachine instance;

    public static Block vendingMachine = new BlockVendingMachine();

    public static boolean isNeiLoaded = false;
    public static boolean isBqLoaded = false;

    @SidedProxy(
        clientSide = "com.cubefury.vendingmachine.ClientProxy",
        serverSide = "com.cubefury.vendingmachine.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        GameRegistry.registerBlock(vendingMachine, "vending_machine");
        GameRegistry.registerTileEntity(TileVendingMachine.class, "vending_machine");

        GameRegistry.registerItem(ItemPlaceholder.placeholder, "placeholder");
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        isNeiLoaded = Loader.isModLoaded("NotEnoughItems");
        isBqLoaded = Loader.isModLoaded("BetterQuesting");
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void missingMapping(FMLMissingMappingsEvent event) {
        for (MissingMapping mapping : event.getAll()) {
            if (mapping.type != Type.BLOCK) {
                continue;
            }
        }
    }
}
