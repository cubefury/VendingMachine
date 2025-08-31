package com.cubefury.vendingmachine.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.cubefury.vendingmachine.util.Translator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockVendingMachine extends ItemBlock {

    public ItemBlockVendingMachine(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean f3_h) {
        tooltip.add("");
        tooltip.add("§o" + Translator.translate("tooltip.vendingmachine"));
    }

}
