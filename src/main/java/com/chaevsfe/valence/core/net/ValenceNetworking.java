package com.chaevsfe.valence.core.net;

import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.modules.inventoryactions.SortLogic;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ValenceNetworking
{
    private ValenceNetworking () { }

    public static void register () {
        PayloadTypeRegistry.clientboundPlay().register(HelloPayload.TYPE, HelloPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SortPayload.TYPE, SortPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            if (ServerPlayNetworking.canSend(listener, HelloPayload.TYPE))
                sender.sendPacket(new HelloPayload(HelloPayload.PROTOCOL, enabledIds()));
        });

        ServerPlayNetworking.registerGlobalReceiver(SortPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            AbstractContainerMenu menu = player.containerMenu;
            if (!Modules.isEnabled("inventory_actions") || menu.containerId != payload.containerId())
                return;
            List<Integer> region = payload.playerSide()
                ? SortLogic.playerRegion(menu, player, false)
                : SortLogic.containerRegion(menu, player);
            if (!region.isEmpty())
                SortLogic.sortRegion(menu, region, player);
        });
    }

    private static List<String> enabledIds () {
        return Modules.enabledMap().entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();
    }
}
