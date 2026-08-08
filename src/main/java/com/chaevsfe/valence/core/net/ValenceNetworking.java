package com.chaevsfe.valence.core.net;

import com.chaevsfe.valence.core.module.Modules;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ValenceNetworking
{
    private ValenceNetworking () { }

    public static void register () {
        PayloadTypeRegistry.clientboundPlay().register(HelloPayload.TYPE, HelloPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            if (ServerPlayNetworking.canSend(listener, HelloPayload.TYPE))
                sender.sendPacket(new HelloPayload(HelloPayload.PROTOCOL, enabledIds()));
        });
    }

    private static List<String> enabledIds () {
        return Modules.enabledMap().entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();
    }
}
