package com.cubefury.vendingmachine.handlers;

import java.util.ArrayDeque;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

import org.apache.commons.lang3.Validate;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.events.MarkDirtyDbEvent;
import com.cubefury.vendingmachine.events.MarkDirtyNamesEvent;
import com.cubefury.vendingmachine.network.handlers.NetBulkSync;
import com.cubefury.vendingmachine.network.handlers.NetTradeDbSync;
import com.cubefury.vendingmachine.storage.NameCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class EventHandler {

    public static final EventHandler INSTANCE = new EventHandler();

    private static final ArrayDeque<FutureTask> serverTasks = new ArrayDeque<>();
    private static Thread serverThread = null;
    private final ArrayDeque<EntityPlayerMP> opQueue = new ArrayDeque<>();
    private boolean openToLAN = false;

    @SubscribeEvent
    public void onMarkDirtyDb(MarkDirtyDbEvent event) {
        SaveLoadHandler.INSTANCE.writeDatabase();
        NetTradeDbSync.sendDatabase(null, false);
    }

    @SubscribeEvent
    public void onMarkDirtyNames(MarkDirtyNamesEvent event) {
        SaveLoadHandler.INSTANCE.writeNames();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (
            event.player.worldObj.isRemote || MinecraftServer.getServer() == null
                || !(event.player instanceof EntityPlayerMP)
        ) return;

        EntityPlayerMP mpPlayer = (EntityPlayerMP) event.player;

        if (
            VendingMachine.proxy.isClient() && !MinecraftServer.getServer()
                .isDedicatedServer()
                && MinecraftServer.getServer()
                    .getServerOwner()
                    .equals(
                        event.player.getGameProfile()
                            .getName())
        ) {
            NameCache.INSTANCE.updateName(mpPlayer);
            return;
        }

        NetBulkSync.sendReset(mpPlayer, true, true);

        UUID playerId = NameCache.INSTANCE.getUUIDFromPlayer(mpPlayer);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> ListenableFuture<T> scheduleServerTask(Callable<T> task) {
        Validate.notNull(task);

        if (Thread.currentThread() != serverThread) {
            ListenableFutureTask<T> lft = ListenableFutureTask.create(task);

            synchronized (serverTasks) {
                serverTasks.add(lft);
                return lft;
            }
        } else {
            try {
                return Futures.immediateFuture(task.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (serverThread == null) serverThread = Thread.currentThread();

            synchronized (serverTasks) {
                while (!serverTasks.isEmpty()) serverTasks.poll()
                    .run();
            }

            return;
        }

        MinecraftServer server = MinecraftServer.getServer();

        if (!server.isDedicatedServer()) {
            boolean tmp = openToLAN;
            openToLAN = server instanceof IntegratedServer && ((IntegratedServer) server).getPublic();
            if (openToLAN && !tmp) opQueue.addAll(server.getConfigurationManager().playerEntityList);
        } else if (!openToLAN) {
            openToLAN = true;
        }

        while (!opQueue.isEmpty()) {
            EntityPlayerMP playerMP = opQueue.poll();
            if (playerMP != null) {
                NameCache.INSTANCE.updateName(playerMP);
            }
        }
    }

}
