package com.chaevsfe.valence.modules.reacharound;

import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.reacharound.client.ReacharoundClient;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public class Reacharound extends ValenceModule
{
    public Reacharound () {
        super("reacharound", ModuleCategory.PLACEMENT, ModuleSide.SERVER_LINKED,
            "Place blocks onto hidden faces past a ledge edge");
    }

    @Override
    public boolean enabledByDefault () {
        return !FabricLoader.getInstance().isModLoaded("tweakeroo");
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.doubleOf("pitch_threshold", 25.0, 0.0, 60.0, "Minimum downward pitch in degrees before reacharound arms")
            .bool("indicator", true, "Show crosshair brackets while reacharound is armed");
    }

    @Override
    public Supplier<ClientModule> client () {
        return () -> new ReacharoundClient(this);
    }
}
