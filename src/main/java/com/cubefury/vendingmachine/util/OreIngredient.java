package com.cubefury.vendingmachine.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

// Cut down version from 1.12
public class OreIngredient {

    private final List<ItemStack> ores;
    private ItemStack[] array = null;
    private int lastSizeA = -1;

    public OreIngredient(String ore) {
        ores = OreDictionary.getOres(ore);
    }

    public boolean apply(@Nullable ItemStack input) {
        if (input == null) return false;

        for (ItemStack target : this.ores) if (OreDictionary.itemMatches(target, input, false)) return true;

        return false;
    }

    @Nonnull
    public ItemStack[] getMatchingStacks() {
        if (array == null || this.lastSizeA != ores.size()) {
            List<ItemStack> lst = new ArrayList<>();
            for (ItemStack itemstack : this.ores) {
                if (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE) itemstack.getItem()
                    .getSubItems(itemstack.getItem(), CreativeTabs.tabAllSearch, lst);
                else lst.add(itemstack.copy());
            }
            this.array = lst.toArray(new ItemStack[0]);
            this.lastSizeA = ores.size();
        }
        return this.array;
    }
}
