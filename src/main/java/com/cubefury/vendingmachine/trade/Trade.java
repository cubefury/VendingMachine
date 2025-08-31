package com.cubefury.vendingmachine.trade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.integration.betterquesting.gui.PanelQBTrade;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.ItemPlaceholder;
import com.cubefury.vendingmachine.util.JsonHelper;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Trade {

    public final List<BigItemStack> fromItems = new ArrayList<>();
    public final List<BigItemStack> toItems = new ArrayList<>();
    public BigItemStack displayItem = new BigItemStack(ItemPlaceholder.placeholder);

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("displayItem", displayItem.writeToNBT(new NBTTagCompound()));
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

    @Optional.Method(modid = "betterquesting")
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTradeGui(IGuiRect rect) {
        return new PanelQBTrade(rect, this);
    }
}
