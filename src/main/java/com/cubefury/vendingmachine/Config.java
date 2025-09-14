package com.cubefury.vendingmachine;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static final String CONFIG_CATEGORY_VM = "Vending Machine Settings";

    public static String data_dir = "vendingmachine";
    public static String config_dir = "config/vendingmachine";
    public static int gui_refresh_interval = 20;
    public static int dispense_frequency = 10;
    public static int dispense_amount = 16;

    public static File worldDir = null;

    public static void init(File configFile) {
        Configuration configuration = new Configuration(configFile);

        data_dir = configuration
            .getString("data_dir", Configuration.CATEGORY_GENERAL, data_dir, "World vendingmachine data directory");
        config_dir = configuration
            .getString("config_dir", Configuration.CATEGORY_GENERAL, config_dir, "Configuration directory");

        configuration.addCustomCategoryComment(CONFIG_CATEGORY_VM, "Vending Machine Settings");
        gui_refresh_interval = configuration
            .getInt("gui_refresh_interval", CONFIG_CATEGORY_VM, gui_refresh_interval, 20, 3600, "In number of ticks");
        dispense_frequency = configuration
            .getInt("dispense_frequency", CONFIG_CATEGORY_VM, dispense_frequency, 1, 9000, "In number of ticks");
        dispense_amount = configuration.getInt(
            "dispense_amount",
            CONFIG_CATEGORY_VM,
            dispense_amount,
            1,
            Integer.MAX_VALUE,
            "Number of items per dispense cycle");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
