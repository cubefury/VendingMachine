package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.util.Translator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CurrencyItem {

    public CurrencyType type;
    public int value;
    private static final Map<String, CurrencyType> typeMap = new HashMap<>();
    private static final String[] coinSuffixes = new String[] { "IV", "III", "II", "I", "" };
    private static final int[] coinValues = new int[] { 10000, 1000, 100, 10, 1 };

    public static CurrencyItem fromNBT(NBTTagCompound nbt) {
        CurrencyType type = CurrencyType.getTypeFromId(nbt.getString("type"));
        if (type == null) {
            return null;
        }
        return new CurrencyItem(type, nbt.getInteger("value"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound payload) {
        payload.setString("type", this.type.id);
        payload.setInteger("value", this.value);
        return payload;
    }

    public List<ItemStack> itemize() {
        List<ItemStack> outputs = new ArrayList<>();
        if (this.type == null || this.value <= 0) {
            return outputs;
        }
        for (int i = 0; i < coinValues.length; i++) {
            while (this.value > coinValues[i]) {
                Item outputItem = (Item) Item.itemRegistry.getObject(this.type.itemPrefix + coinSuffixes[i]);
                int stackSize = Math.min(this.value / coinValues[i], outputItem.getItemStackLimit());
                outputs.add(new ItemStack(outputItem, stackSize));
                this.value -= stackSize * coinValues[i];
            }
        }
        return outputs;
    }

    public enum CurrencyType {

        ADVENTURE("adventure", "dreamcraft:item.CoinAdventure", "gui/icons/itemCoinAdventure.png"),
        BEES("bees", "dreamcraft:item.CoinBees", "gui/icons/itemCoinBees.png"),
        BLOOD("blood", "dreamcraft:item.CoinBlood", "gui/icons/itemCoinBlood.png"),
        CHEMIST("chemist", "dreamcraft:item.CoinChemist", "gui/icons/itemCoinChemist.png"),
        COOK("cook", "dreamcraft:item.CoinCook", "gui/icons/itemCoinCook.png"),
        DARK_WIZARD("darkWizard", "dreamcraft:item.CoinDarkWizard", "gui/icons/itemCoinDarkWizard.png"),
        FARMER("farmer", "dreamcraft:item.CoinFarmer", "gui/icons/itemCoinFarmer.png"),
        FLOWER("flower", "dreamcraft:item.CoinFlower", "gui/icons/itemCoinFlower.png"),
        FORESTRY("forestry", "dreamcraft:item.CoinForestry", "gui/icons/itemCoinForestry.png"),
        SMITH("smith", "dreamcraft:item.CoinSmith", "gui/icons/itemCoinSmith.png"),
        SPACE("space", "dreamcraft:item.CoinSpace", "gui/icons/itemCoinSpace.png"),
        SURVIVOR("survivor", "dreamcraft:item.CoinSurvivor", "gui/icons/itemCoinSurvivor.png"),
        TECHNICIAN("technician", "dreamcraft:item.CoinTechnician", "gui/icons/itemCoinTechnician.png"),
        WITCH("witch", "dreamcraft:item.CoinWitch", "gui/icons/itemCoinWitch.png"),
        // comment before semicolon to reduce merge conflicts
        ;

        public final String id;
        public final String itemPrefix;
        public final UITexture texture;

        CurrencyType(String id, String itemPrefix, String texture) {
            this.id = id;
            this.itemPrefix = itemPrefix;
            this.texture = UITexture.builder()
                .location(VendingMachine.MODID, texture)
                .imageSize(32, 32)
                .name("VM_UI_Coin_" + id)
                .build();

            typeMap.put(this.id, this);
        }

        public static CurrencyType getTypeFromId(String type) {
            return typeMap.get(type);
        }

        @SideOnly(Side.CLIENT)
        public String getLocalizedName() {
            return Translator.translate("vendingmachine.coin." + this.id);
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
        for (int i = 0; i < coinSuffixes.length; i++) {
            if (suffix.equals(coinSuffixes[i])) {
                return coinValues[i];
            }
        }
        return -1;
    }
}
