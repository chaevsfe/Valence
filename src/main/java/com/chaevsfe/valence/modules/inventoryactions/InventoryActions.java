package com.chaevsfe.valence.modules.inventoryactions;

import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.inventoryactions.client.InventoryActionsClient;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public class InventoryActions extends ValenceModule
{
    private static final List<String> SORTER_MODS = List.of("inventoryprofilesnext", "mousewheelie", "inventorysorter");

    public InventoryActions () {
        super("inventory_actions", ModuleCategory.INTERFACE, ModuleSide.CLIENT_ONLY,
            "Sort, deposit and extract buttons on container screens");
    }

    @Override
    public boolean enabledByDefault () {
        return SORTER_MODS.stream().noneMatch(id -> FabricLoader.getInstance().isModLoaded(id));
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.bool("screen_buttons", true, "Show sort, deposit and extract buttons on container screens")
            .intOf("clicks_per_tick", 4, 1, 20, "Inventory clicks sent per tick while an action runs");
    }

    @Override
    public Supplier<ClientModule> client () {
        return () -> new InventoryActionsClient(this);
    }
}
