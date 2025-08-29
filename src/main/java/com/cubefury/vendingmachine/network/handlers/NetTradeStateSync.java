package com.cubefury.vendingmachine.network.handlers;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.network.UnserializedPacket;
import com.cubefury.vendingmachine.api.util.Tuple2;
import com.cubefury.vendingmachine.network.PacketSender;
import com.cubefury.vendingmachine.network.PacketTypeRegistry;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.Trade;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeGroup;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.JsonHelper;
import com.cubefury.vendingmachine.util.NBTConverter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NetTradeStateSync {

    private static final ResourceLocation ID_NAME = new ResourceLocation("vendingmachine:tradestate_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetTradeStateSync::onServer);

        if (VendingMachine.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetTradeStateSync::onClient);
        }
    }

    // server side code for sending tradegroup data when player opens gui
    public static void sendTradeState(@Nullable EntityPlayerMP player, boolean merge) {
        TradeDatabase db = TradeDatabase.INSTANCE;
        UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(player);

        NBTTagCompound payload = new NBTTagCompound();
        payload.setBoolean("merge", merge);
        NBTConverter.UuidValueType.PLAYER.writeId(playerId, payload);

        db.writeTradeStateToNBT(new NBTTagCompound(), playerId);

        if (player == null) { // shouldn't happen, since we're only updating one player
            PacketSender.INSTANCE.sendToAll(new UnserializedPacket(ID_NAME, payload));
        } else {
            PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void getTrades() {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("requestType", "getTrades");

        PacketSender.INSTANCE.sendToServer(new UnserializedPacket(ID_NAME, payload));
    }

    @SideOnly(Side.CLIENT)
    public static void claimTrade(UUID tradeGroup, Trade trade) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("requestType", "claimTrade");
        NBTConverter.UuidValueType.TRADEGROUP.writeId(tradeGroup, payload);

        NBTTagList pendingOutput = new NBTTagList();
        for (BigItemStack stack : trade.toItems) {
            pendingOutput.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
        }
        payload.setTag("pendingOutput", pendingOutput);

        PacketSender.INSTANCE.sendToServer(new UnserializedPacket(ID_NAME, payload));
    }

    public static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        TradeDatabase db = TradeDatabase.INSTANCE;
        String requestType = message.first()
            .getString("requestType");
        switch (requestType) {
            case "claimTrade":
                UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(message.second());
                NBTTagList pendingOutput = message.first()
                    .getTagList("pendingOutput", Constants.NBT.TAG_COMPOUND);
                TradeGroup tg = db.getTradeGroupFromId(NBTConverter.UuidValueType.TRADEGROUP.readId(message.first()));
                if (tg.attemptExecuteTrade(playerId)) {
                    NetTradeOutputSync.sendReward(message.second(), pendingOutput);
                    sendTradeState(message.second(), false);
                } else {
                    VendingMachine.LOG
                        .warn("Player {} made invalid reward claim attempt for trade group {}", playerId, tg.getId());
                }
                break; // technically we can let this continue cuz we sendTradeState in both cases...
            case "getTrades":
                sendTradeState(message.second(), false);
                break;
            default:
                VendingMachine.LOG.warn("Unknown trade state sync request type received: {}", requestType);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onClient(NBTTagCompound message) {
        // Don't wipe everyone else's data if on LAN, since
        // we receive only the requested player's data
        // In SP and LAN, we'll have this data already anyway
        if (
            Minecraft.getMinecraft()
                .isIntegratedServerRunning()
        ) {
            return;
        }
        TradeDatabase db = TradeDatabase.INSTANCE;
        UUID player = NBTConverter.UuidValueType.PLAYER.readId(message);
        db.populateTradeStateFromNBT(message, player, message.getBoolean("merge"));
    }
}
