package com.cubefury.vendingmachine.network.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.network.UnserializedPacket;
import com.cubefury.vendingmachine.api.util.Tuple2;
import com.cubefury.vendingmachine.network.PacketSender;
import com.cubefury.vendingmachine.network.PacketTypeRegistry;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.TradeManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NetAvailableTradeSync {

    private static final ResourceLocation ID_NAME = new ResourceLocation("vendingmachine:availabletrade_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetAvailableTradeSync::onServer);

        if (VendingMachine.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetAvailableTradeSync::onClient);
        }
    }

    public static void sendSync(@Nullable EntityPlayerMP player) {
        NBTTagList tradeGroups = new NBTTagList();
        for (UUID available : TradeManager.INSTANCE.getAvailableTrades(NameCache.INSTANCE.getUUIDFromPlayer(player))) {
            tradeGroups.appendTag(new NBTTagString(available.toString()));
        }
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("tradeGroups", tradeGroups);
        PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);

    }

    @SideOnly(Side.CLIENT)
    public static void requestSync() {
        PacketSender.INSTANCE.sendToServer(new UnserializedPacket(ID_NAME, new NBTTagCompound()));
    }

    public static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        sendSync(message.second());
    }

    @SideOnly(Side.CLIENT)
    public static void onClient(NBTTagCompound message) {
        if ( // Don't sync in LAN - will delete other player's data
        Minecraft.getMinecraft()
            .isIntegratedServerRunning()
        ) {
            return;
        }
        UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(Minecraft.getMinecraft().thePlayer);
        Set<UUID> tradeGroups = new HashSet<>();
        NBTTagList tradeList = message.getTagList("tradeGroups", Constants.NBT.TAG_STRING);
        for (int i = 0; i < tradeList.tagCount(); i++) {
            tradeGroups.add(UUID.fromString(tradeList.getStringTagAt(i)));
        }
        TradeManager.INSTANCE.setAvailableTrades(playerId, tradeGroups);
    }
}
