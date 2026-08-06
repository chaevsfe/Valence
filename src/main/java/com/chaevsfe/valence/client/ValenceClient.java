package com.chaevsfe.valence.client;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.core.module.ValenceModule;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;

public class ValenceClient implements ClientModInitializer
{
    public static KeyMapping.Category KEY_CATEGORY;

    @Override
    public void onInitializeClient () {
        KEY_CATEGORY = KeyMapping.Category.register(ModConstants.loc("valence"));
        for (ValenceModule m : Modules.ALL) {
            Supplier<ClientModule> client = m.client();
            if (client != null)
                client.get().initClient();
        }
    }
}
