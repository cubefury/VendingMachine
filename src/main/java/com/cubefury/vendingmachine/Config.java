package com.cubefury.vendingmachine;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static final String CONFIG_CATEGORY_GUI = "GUI";

    public static String data_dir = "vendingmachine";
    public static String config_dir = "config/vendingmachine";
    public static int gui_refresh_interval = 20;

    public static File worldDir = null;

    public static void init(File configFile) {
        Configuration configuration = new Configuration(configFile);

        data_dir = configuration
            .getString("data_dir", Configuration.CATEGORY_GENERAL, data_dir, "World vendingmachine data directory");
        config_dir = configuration
            .getString("config_dir", Configuration.CATEGORY_GENERAL, config_dir, "Configuration directory");

        configuration.addCustomCategoryComment(CONFIG_CATEGORY_GUI, "GUI Settings");
        gui_refresh_interval = configuration
            .getInt("gui_refresh_interval", CONFIG_CATEGORY_GUI, gui_refresh_interval, 20, 3600, "In number of ticks");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
