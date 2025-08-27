package com.cubefury.vendingmachine.handlers;

import static com.cubefury.vendingmachine.util.FileIO.CopyPaste;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
                List<File> fList = new ArrayList<>();
                for (File f : fileList) {
                    if (
                        f.getName()
                            .endsWith(".json")
                    ) {
                        fList.add(f);
                    }
                }
                JsonHelper.populateTradeStateFromFiles(fList);
            } else {
                JsonHelper.populateTradeStateFromFiles(new ArrayList<>());
            }
        }
    }

    public void writeTradeState(Collection<UUID> players) {

    }
}
