package com.chaevsfe.valence.modules.placement;

import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.placement.client.FlexiblePlacementClient;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public class FlexiblePlacement extends ValenceModule
{
    public FlexiblePlacement () {
        super("flexible_placement", ModuleCategory.PLACEMENT, ModuleSide.CLIENT_ONLY,
            "Hold a key to choose the orientation of the block you are placing");
    }

    @Override
    public boolean enabledByDefault () {
        return !FabricLoader.getInstance().isModLoaded("tweakeroo");
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.bool("indicator", true, "Show the chosen orientation beside the crosshair");
    }

    @Override
    public Supplier<ClientModule> client () {
        return () -> new FlexiblePlacementClient(this);
    }
}
