package com.chaevsfe.valence;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.ModServices;
import com.chaevsfe.valence.core.config.ConfigSnapshot;
import com.chaevsfe.valence.core.config.ValenceConfig;
import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.core.module.ValenceModule;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Valence implements ModInitializer
{
    @Override
    public void onInitialize () {
        ConfigSnapshot snap = ValenceConfig.load(
            FabricLoader.getInstance().getConfigDir().resolve(ModConstants.MOD_ID + ".json5"),
            Modules.schema());
        Modules.applyConfig(snap);

        for (ValenceModule m : Modules.ALL)
            m.register();
        for (ValenceModule m : Modules.ALL)
            m.init();

        ModServices.LOG.info("{} modules loaded", Modules.ALL.size());
    }
}
