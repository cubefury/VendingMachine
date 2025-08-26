package com.cubefury.vendingmachine.handlers;

import static com.cubefury.vendingmachine.util.FileIO.CopyPaste;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;

import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.util.JsonHelper;

public class SaveLoadHandler {

    public static SaveLoadHandler INSTANCE = new SaveLoadHandler();

    private File defaultFileDatabase = null;
    private File fileDatabase = null;
    private File dirTradeState = null;

    private SaveLoadHandler() {}

    public void init(MinecraftServer server) {
        if (VendingMachine.proxy.isClient()) {
            Config.worldDir = server.getFile("saves/" + server.getFolderName() + "/" + Config.data_dir);
        } else {
            Config.worldDir = server.getFile(server.getFolderName() + "/" + Config.data_dir);
        }

        defaultFileDatabase = new File(Config.config_dir, "tradeDatabase.json");
        dirTradeState = new File(Config.worldDir, "tradeState");

        loadDatabase();
        loadTradeState();
    }

    public void loadDatabase() {
        JsonHelper.populateTradeDatabaseFromFile(fileDatabase);
    }

    public void loadTradeState() {
        if (dirTradeState.exists()) {
            CopyPaste(dirTradeState, new File(Config.worldDir + "/backup", "dirTradeState"));
            // JsonHelper.populateTradeStateFromFile();
        }
    }

    public void writeTradeState(Collection<UUID> players) {

    }
}
