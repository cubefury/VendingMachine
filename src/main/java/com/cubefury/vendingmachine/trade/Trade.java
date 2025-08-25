package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.integration.betterquesting.gui.PanelQBTrade;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.JsonHelper;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Trade {

    public final List<BigItemStack> fromItems = new ArrayList<>();
    public final List<BigItemStack> toItems = new ArrayList<>();
    public boolean ignoreInputsNbt = true;

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("ignoreInputsNBT", ignoreInputsNbt);

        NBTTagList fromItemsArray = new NBTTagList();
        for (BigItemStack stack : this.fromItems) {
            fromItemsArray.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
        }
        nbt.setTag("fromItems", fromItemsArray);

        NBTTagList toItemsArray = new NBTTagList();
        for (BigItemStack stack : this.toItems) {
            toItemsArray.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
        }
        nbt.setTag("toItems", toItemsArray);

        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        ignoreInputsNbt = nbt.getBoolean("ignoreInputsNBT");

        fromItems.clear();
        toItems.clear();
        NBTTagList fromList = nbt.getTagList("fromItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fromList.tagCount(); i++) {
            fromItems.add(JsonHelper.JsonToItemStack(fromList.getCompoundTagAt(i)));
        }

        NBTTagList toList = nbt.getTagList("toItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < toList.tagCount(); i++) {
            toItems.add(JsonHelper.JsonToItemStack(toList.getCompoundTagAt(i)));
        }
    }

    public void executeTrade(EntityPlayer player) {}

    @Optional.Method(modid = "NotEnoughItems")
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTradeGui(IGuiRect rect) {
        return new PanelQBTrade(rect, this);
    }
}
