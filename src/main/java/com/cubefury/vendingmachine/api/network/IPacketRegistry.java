package com.cubefury.vendingmachine.api.network;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.cubefury.vendingmachine.api.util.Tuple2;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IPacketRegistry {

    void registerServerHandler(@Nonnull ResourceLocation idName,
        @Nonnull Consumer<Tuple2<NBTTagCompound, EntityPlayerMP>> method);

    @SideOnly(Side.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<NBTTagCompound> method);

    @Nullable
    Consumer<Tuple2<NBTTagCompound, EntityPlayerMP>> getServerHandler(@Nonnull ResourceLocation idName);

    @Nullable
    @SideOnly(Side.CLIENT)
    Consumer<NBTTagCompound> getClientHandler(@Nonnull ResourceLocation idName);
}
