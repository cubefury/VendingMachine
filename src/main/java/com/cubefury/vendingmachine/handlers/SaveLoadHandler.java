package com.cubefury.vendingmachine.handlers;

import static com.cubefury.vendingmachine.util.FileIO.CopyPaste;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import com.cubefury.vendingmachine.Config;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.util.FileIO;
import com.cubefury.vendingmachine.util.JsonHelper;
import com.cubefury.vendingmachine.util.NBTConverter;

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

        if (dirTradeState.mkdirs()) {
            VendingMachine.LOG.info("Created trade state directory");
        }

        loadDatabase();
        loadTradeState();
    }

    public void loadDatabase() {
        CopyPaste(fileDatabase, new File(Config.config_dir + "/backup", "tradeDatabase.json"));
        JsonHelper.populateTradeDatabaseFromFile(fileDatabase);
    }

    public void writeDatabase() {
        CopyPaste(fileDatabase, new File(Config.config_dir + "/backup", "tradeDatabase.json"));
        FileIO.WriteToFile(
            fileDatabase,
            out -> NBTConverter.NBTtoJSON_Compound(TradeDatabase.INSTANCE.writeToNBT(new NBTTagCompound()), out, true));
    }

    public void loadTradeState() {
        if (dirTradeState.exists()) {
            CopyPaste(dirTradeState, new File(Config.worldDir + "/backup", "tradeState"));
            File[] fileList = dirTradeState.listFiles();

            if (fileList != null) {
                JsonHelper.populateTradeStateFromFiles(
                    Arrays.stream(fileList)
                        .filter(
                            f -> f.getName()
                                .endsWith(".json"))
                        .collect(Collectors.toList()));
            } else {
                JsonHelper.populateTradeStateFromFiles(new ArrayList<>());
            }
        }
    }

    public void writeTradeState(Collection<UUID> players) {
        TradeDatabase db = TradeDatabase.INSTANCE;
        for (UUID player : players) {
            File playerFile = new File(dirTradeState, player.toString() + ".json");
            CopyPaste(playerFile, new File(Config.worldDir + "/backup", player.toString() + ".json"));
            NBTTagCompound state = db.writeTradeStateToNBT(new NBTTagCompound(), player);
            FileIO.WriteToFile(playerFile, out -> NBTConverter.NBTtoJSON_Compound(state, out, true));
        }
    }

}
