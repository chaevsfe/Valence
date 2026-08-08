package com.chaevsfe.valence.client;

import com.chaevsfe.valence.client.gui.ValenceConfigScreen;
import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.core.net.HelloPayload;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ValenceClient implements ClientModInitializer
{
    public static KeyMapping.Category KEY_CATEGORY;

    @Override
    public void onInitializeClient () {
        KEY_CATEGORY = KeyMapping.Category.register(ModConstants.loc("valence"));
        ClientPlayNetworking.registerGlobalReceiver(HelloPayload.TYPE,
            (payload, context) -> ServerSession.accept(payload.enabled()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerSession.clear());
        for (ValenceModule m : Modules.ALL) {
            Supplier<ClientModule> client = m.client();
            if (client != null)
                client.get().initClient();
        }

        KeyMapping configKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.valence.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick())
                ValenceConfigScreen.open(client, new ValenceConfigScreen(null));
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen || screen instanceof PauseScreen)
                Screens.getWidgets(screen).add(Button.builder(Component.literal("Valence"),
                        button -> ValenceConfigScreen.open(client, new ValenceConfigScreen(screen)))
                    .bounds(4, scaledHeight - 24, 58, 20)
                    .build());
        });
    }
}
