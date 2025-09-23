package com.cubefury.vendingmachine.network.handlers;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.api.network.UnserializedPacket;
import com.cubefury.vendingmachine.api.util.Tuple2;
import com.cubefury.vendingmachine.network.PacketSender;
import com.cubefury.vendingmachine.network.PacketTypeRegistry;
import com.cubefury.vendingmachine.storage.NameCache;
import com.cubefury.vendingmachine.trade.CurrencyItem;
import com.cubefury.vendingmachine.trade.TradeDatabase;
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.NBTConverter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NetTradeStateSync {

    // We can send tradestate + currency data, or just currency data
    // The latter is far more lightweight, and will likely see higher traffic volume
    private static final ResourceLocation ID_NAME = new ResourceLocation("vendingmachine:tradestate_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetTradeStateSync::onServer);

        if (VendingMachine.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetTradeStateSync::onClient);
        }
    }

    // server side code for sending tradegroup data when player opens gui
    public static void sendTradeState(@Nonnull EntityPlayerMP player, boolean merge) {
        TradeDatabase db = TradeDatabase.INSTANCE;
        UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(player);

        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("dataType", "tradeState");
        payload.setBoolean("merge", merge);
        NBTConverter.UuidValueType.PLAYER.writeId(playerId, payload);

        db.writeTradeStateToNBT(new NBTTagCompound(), playerId);

        PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);
    }

    public static void sendPlayerCurrency(@Nonnull EntityPlayerMP player, CurrencyItem currencyItem) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("dataType", "currency_add");
        currencyItem.writeToNBT(payload);

        PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);
    }

    public static void resetPlayerCurrency(@Nonnull EntityPlayerMP player, @Nullable CurrencyItem.CurrencyType type) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("dataType", "currency_reset");
        if (type != null) {
            payload.setString("type", type.id);
        }
        PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);
    }

    @SideOnly(Side.CLIENT)
    public static void requestSync() {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("requestType", "getTradeState");

        PacketSender.INSTANCE.sendToServer(new UnserializedPacket(ID_NAME, payload));
    }

    public static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        String requestType = message.first()
            .getString("requestType");
        if (requestType.equals("getTradeState")) {
            sendTradeState(message.second(), false);
        } else {
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
        String dataType = message.getString("dataType");
        UUID player = NBTConverter.UuidValueType.PLAYER.readId(message);
        switch (dataType) {
            case "tradeState" -> {
                boolean merge = message.getBoolean("merge");
                TradeDatabase.INSTANCE.populateTradeStateFromNBT(message, player, merge);
            }
            case "currency_add" -> {
                CurrencyItem currencyItem = CurrencyItem.fromNBT(message.getCompoundTag("currencyItem"));

                TradeManager.INSTANCE.addCurrency(player, currencyItem);
            }
            case "currency_reset" -> TradeManager.INSTANCE.resetCurrency(
                player,
                message.hasKey("type") ? CurrencyItem.CurrencyType.getTypeFromId(message.getString("type")) : null);
            default -> VendingMachine.LOG.warn("Unknown trade state sync data received: {}", dataType);
        }
    }
}
