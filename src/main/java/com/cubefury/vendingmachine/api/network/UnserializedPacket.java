package com.cubefury.vendingmachine.api.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record UnserializedPacket(ResourceLocation handler, NBTTagCompound payload) {}
