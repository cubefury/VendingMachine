package com.cubefury.vendingmachine.util;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * The BigItemStack serialization and deserialization code was taken from BetterQuesting's implementation of
 * utils.JsonHelper.
 * We do not reference the class directly, so BetterQuesting may remain as an optional dependency.
 * BetterQuesting's license may be found in the root directory of this project.
 */
public class JsonHelper {

    public static BigItemStack JsonToItemStack(@Nonnull NBTTagCompound nbt) {
        String idName = nbt.hasKey("id", Constants.NBT.TAG_ANY_NUMERIC) ? "" + nbt.getShort("id") : nbt.getString("id");
        Item preCheck = nbt.hasKey("id", Constants.NBT.TAG_ANY_NUMERIC) ? Item.getItemById(nbt.getShort("id"))
            : (Item) Item.itemRegistry.getObject(idName);
        if (preCheck == null && nbt.hasKey("id", Constants.NBT.TAG_STRING)) {
            try {
                preCheck = Item.getItemById(Short.parseShort(idName));
            } catch (Exception ignored) {}
        }

        if (preCheck != null && preCheck != ItemPlaceholder.placeholder) { // valid item
            return BigItemStack.loadItemStackFromNBT(nbt);
        }
        return ItemPlaceholder.getBigItemStackFrom(
            preCheck,
            idName,
            nbt.getInteger("Count"),
            nbt.getShort("Damage"),
            nbt.getString("OreDict"),
            !nbt.hasKey("tag", Constants.NBT.TAG_COMPOUND) ? null : nbt.getCompoundTag("tag"));
    }

    // We use this instead of converting ItemStack to NBT since this doesn't use ID numbers
    public static NBTTagCompound ItemStackToJson(BigItemStack stack, NBTTagCompound nbt) {
        if (stack != null) {
            return stack.writeToNBT(nbt);
        }
        return nbt;
    }
}
