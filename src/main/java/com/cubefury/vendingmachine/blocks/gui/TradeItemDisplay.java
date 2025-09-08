package com.cubefury.vendingmachine.blocks.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.NBTConverter;

import codechicken.nei.api.ItemFilter;

public class TradeItemDisplay {

    public List<BigItemStack> fromItems;
    public List<BigItemStack> toItems;
    public ItemStack display;
    public UUID tgID;
    public int tradeGroupOrder;
    public String label;
    public long cooldown;
    public String cooldownText;
    public boolean hasCooldown;
    public boolean enabled;
    public boolean tradeableNow;
    public UUID playerID; // used to identify player who claimed a trade on the server

    public TradeItemDisplay(List<BigItemStack> fromItems, List<BigItemStack> toItems, ItemStack display, UUID tgID,
        int tradeGroupOrder, String label, long cooldown, String cooldownText, boolean hasCooldown, boolean enabled,
        boolean tradeableNow, UUID playerID) {
        this.fromItems = fromItems;
        this.toItems = toItems;
        this.display = display;
        this.tgID = tgID;
        this.tradeGroupOrder = tradeGroupOrder;
        this.label = label;
        this.cooldown = cooldown;
        this.cooldownText = cooldownText;
        this.hasCooldown = hasCooldown;
        this.enabled = enabled;
        this.tradeableNow = tradeableNow;
        this.playerID = playerID;
    }

    public static TradeItemDisplay readFromNBT(NBTTagCompound nbt) {
        List<BigItemStack> newFromItems = new ArrayList<>();
        NBTTagList fromItemsList = nbt.getTagList("fromItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fromItemsList.tagCount(); i++) {
            newFromItems.add(BigItemStack.loadItemStackFromNBT(fromItemsList.getCompoundTagAt(i)));
        }
        List<BigItemStack> newToItems = new ArrayList<>();
        NBTTagList toItemsList = nbt.getTagList("toItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fromItemsList.tagCount(); i++) {
            newToItems.add(BigItemStack.loadItemStackFromNBT(toItemsList.getCompoundTagAt(i)));
        }
        return new TradeItemDisplay(
            newFromItems,
            newToItems,
            ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("display")),
            NBTConverter.UuidValueType.TRADEGROUP.readId(nbt),
            nbt.getInteger("tradeGroupOrder"),
            nbt.getString("label"),
            nbt.getLong("cooldown"),
            nbt.getString("cooldownText"),
            nbt.getBoolean("hasCooldown"),
            nbt.getBoolean("enabled"),
            nbt.getBoolean("tradeableNow"),
            NBTConverter.UuidValueType.PLAYER.readId(nbt));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList fromItemsNBT = new NBTTagList();
        for (BigItemStack bis : this.fromItems) {
            fromItemsNBT.appendTag(bis.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("fromItems", fromItemsNBT);
        NBTTagList toItemsNBT = new NBTTagList();
        for (BigItemStack bis : this.toItems) {
            toItemsNBT.appendTag(bis.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("toItems", toItemsNBT);
        nbt.setTag("display", this.display.writeToNBT(new NBTTagCompound()));
        NBTConverter.UuidValueType.TRADEGROUP.writeId(this.tgID, nbt);
        nbt.setInteger("tradeGroupOrder", this.tradeGroupOrder);
        nbt.setString("label", this.label);
        nbt.setLong("cooldown", this.cooldown);
        nbt.setString("cooldownText", this.cooldownText);
        nbt.setBoolean("hasCooldown", this.hasCooldown);
        nbt.setBoolean("enabled", this.enabled);
        nbt.setBoolean("tradeableNow", this.tradeableNow);
        NBTConverter.UuidValueType.PLAYER.writeId(this.playerID, nbt);

        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TradeItemDisplay)) {
            return false;
        }
        TradeItemDisplay other = (TradeItemDisplay) obj;
        return this.fromItems.equals(other.fromItems) && this.toItems.equals(other.toItems)
            && ItemStack.areItemStacksEqual(this.display, other.display)
            && ItemStack.areItemStackTagsEqual(this.display, other.display)
            && this.tgID == other.tgID
            && this.tradeGroupOrder == other.tradeGroupOrder
            && this.label.equals(other.label)
            && this.cooldown == other.cooldown
            && this.cooldownText.equals(other.cooldownText)
            && this.hasCooldown == other.hasCooldown
            && this.enabled == other.enabled
            && this.tradeableNow == other.tradeableNow
            && this.playerID == other.playerID;
    }

    public boolean satisfiesSearch(ItemFilter filter, String searchStringNoCase) {
        if (filter == null) {
            return this.label.toLowerCase()
                .contains(searchStringNoCase);
        }
        return filter.matches(this.display) || this.toItems.stream()
            .anyMatch(bis -> filter.matches(bis.getBaseStack()))
            || this.fromItems.stream()
                .anyMatch(bis -> filter.matches(bis.getBaseStack()));
    }
}
