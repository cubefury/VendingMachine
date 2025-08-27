package com.cubefury.vendingmachine.handlers;

import static com.cubefury.vendingmachine.util.FileIO.CopyPaste;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.server.MinecraftServer;

import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.util.JsonHelper;

public class SaveLoadHandler {

    public static SaveLoadHandler INSTANCE = new SaveLoadHandler();

    private File fileDatabase = null;
    private File dirTradeState = null;

    private SaveLoadHandler() {}

    public void init(MinecraftServer server) {
        if (VendingMachine.proxy.isClient()) {
            Config.worldDir = server.getFile("saves/" + server.getFolderName() + "/" + Config.data_dir);
        } else {
            Config.worldDir = server.getFile(server.getFolderName() + "/" + Config.data_dir);
        }

        fileDatabase = new File(Config.config_dir, "tradeDatabase.json");
        dirTradeState = new File(Config.worldDir, "tradeState");

        loadDatabase();
        loadTradeState();
    }

    public void loadDatabase() {
        CopyPaste(fileDatabase, new File(Config.config_dir + "/backup", "tradeDatabase.json"));
        JsonHelper.populateTradeDatabaseFromFile(fileDatabase);
    }

    public void writeDatabase() {
        CopyPaste(fileDatabase, new File(Config.config_dir + "/backup", "tradeDatabase.json"));
        FileIO.WriteToFile(fileDatabase,
            out -> NBTConverter.NBTtoJSON_Compound(TradeDatabase.INSTANCE.writeToNBT(new NBTTagCompound()), out, true));
    }
    public void loadTradeState() {
        if (dirTradeState.exists()) {
            CopyPaste(dirTradeState, new File(Config.worldDir + "/backup", "tradeState"));
            JsonHelper.populateTradeStateFromFiles(dirTradeState
                .listFiles()
                .stream()
                .filter(f -> f.getName().endswith(".json"))
                .collect(Collectors::toList));
        }
    }

    public void writeTradeState(Collection<UUID> players) {

    }
}
