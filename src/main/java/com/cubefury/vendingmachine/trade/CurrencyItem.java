package com.cubefury.vendingmachine.trade;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CurrencyItem {

    public CurrencyType type;
    public int value;
    private static final Map<String, CurrencyType> typeMap = new HashMap<>();

    public static CurrencyItem fromNBT(NBTTagCompound nbt) {
        CurrencyType type = CurrencyType.getTypeFromId(nbt.getString("type"));
        if (type == null) {
            return null;
        }
        return new CurrencyItem(type, nbt.getInteger("value"));
    }

    public void writeToNBT(NBTTagCompound payload) {
        payload.setString("type", this.type.id);
        payload.setInteger("value", this.value);
    }

    public enum CurrencyType {

        ADVENTURE("adventure", "dreamcraft:item.CoinAdventure"),
        BEES("bees", "dreamcraft:item.CoinBees"),
        BLOOD("blood", "dreamcraft:item.CoinBlood"),
        CHEMIST("chemist", "dreamcraft:item.CoinChemist"),
        COOK("cook", "dreamcraft:item.CoinCook"),
        DARK_WIZARD("darkWizard", "dreamcraft:item.CoinDarkWizard"),
        FARMER("farmer", "dreamcraft:item.CoinFarmer"),
        FLOWER("flower", "dreamcraft:item.CoinFlower"),
        FORESTRY("forestry", "dreamcraft:item.CoinForestry"),
        SMITH("smith", "dreamcraft:item.CoinSmith"),
        WITCH("witch", "dreamcraft:item.CoinWitch"),
        SPACE("space", "dreamcraft:item.CoinSpace"),
        SURVIVOR("survivor", "dreamcraft:item.CoinSurvivor"),
        TECHNICIAN("technician", "dreamcraft:item.CoinTechnician"),
        // comment before semicolon to reduce merge conflicts
        ;

        public final String id;
        public final String itemPrefix;

        CurrencyType(String id, String itemPrefix) {
            this.id = id;
            this.itemPrefix = itemPrefix;
            typeMap.put(this.id, this);
        }

        public static CurrencyType getTypeFromId(String type) {
            return typeMap.get(type);
        }
    }

    public CurrencyItem(CurrencyType type, int value) {
        this.type = type;
        this.value = value;
    }

    public static CurrencyItem fromItemStack(ItemStack newItem) {
        String itemName = Item.itemRegistry.getNameForObject(newItem.getItem());
        for (CurrencyType type : CurrencyType.values()) {
            if (itemName.startsWith(type.itemPrefix)) {
                int currencyValue = mapSuffixToValue(itemName.substring(type.itemPrefix.length()));
                if (currencyValue < 0) {
                    return null;
                }
                return new CurrencyItem(type, currencyValue * newItem.stackSize);
            }
        }
        return null;
    }

    private static int mapSuffixToValue(String suffix) {
        return switch (suffix) {
            case "" -> 1;
            case "I" -> 10;
            case "II" -> 100;
            case "III" -> 1000;
            case "IV" -> 10000;
            default -> -1;
        };
    }
}
