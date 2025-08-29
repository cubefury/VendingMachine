package com.cubefury.vendingmachine.network.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.BigItemStack;
import com.cubefury.vendingmachine.util.JsonHelper;
import com.cubefury.vendingmachine.util.NBTConverter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NetTradeOutputSync {

    private static final ResourceLocation ID_NAME = new ResourceLocation("vendingmachine:trade_output_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetTradeOutputSync::onServer);

        if (VendingMachine.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetTradeOutputSync::onClient);
        }
    }

    public static void sendReward(EntityPlayerMP player, NBTTagList pending) {
        NBTTagCompound payload = new NBTTagCompound();
        UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(player);
        NBTConverter.UuidValueType.PLAYER.writeId(playerId, payload);
        payload.setTag("pending", pending);

        PacketSender.INSTANCE.sendToPlayers(new UnserializedPacket(ID_NAME, payload), player);
    }

    public static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        VendingMachine.LOG.warn("Impossible trade output sync request received on server...");
    }

    @SideOnly(Side.CLIENT)
    public static void onClient(NBTTagCompound message) {
        UUID playerId = NBTConverter.UuidValueType.PLAYER.readId(message);
        NBTTagList pending = message.getTagList("pending", Constants.NBT.TAG_COMPOUND);
        List<BigItemStack> pendingItems = new ArrayList<>();
        for (int i = 0; i < pending.tagCount(); i++) {
            pendingItems.add(JsonHelper.JsonToItemStack(pending.getCompoundTagAt(i)));
        }
        TradeManager.INSTANCE.addPending(playerId, pendingItems);

        // we let the auto refresh on the Vending Machine
        // container handle the dispensing
    }
}
