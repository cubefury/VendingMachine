package com.cubefury.vendingmachine.integration.nei;

import java.util.ArrayList;
import java.util.List;

import com.cubefury.vendingmachine.Tags;
import com.cubefury.vendingmachine.VendingMachine;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {

    public static List<NeiRecipeHandler> instances = new ArrayList<>();

    @Override
    public void loadConfig() {
        API.registerRecipeHandler(new NeiRecipeHandler());
        API.registerUsageHandler(new NeiRecipeHandler());
    }

    @Override
    public String getName() {
        return VendingMachine.NAME;
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }

}
