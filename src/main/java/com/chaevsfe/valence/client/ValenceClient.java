package com.chaevsfe.valence.client;

import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.core.module.ValenceModule;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;

public class ValenceClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient () {
        for (ValenceModule m : Modules.ALL) {
            Supplier<ClientModule> client = m.client();
            if (client != null)
                client.get().initClient();
        }
    }
}
