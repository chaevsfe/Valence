package com.chaevsfe.valence.modules.helditem;

import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.helditem.client.HeldItemReadoutClient;
import java.util.function.Supplier;

public class HeldItemReadout extends ValenceModule
{
    public HeldItemReadout () {
        super("held_item_readout", ModuleCategory.INTERFACE, ModuleSide.CLIENT_ONLY,
            "Shows durability or count of held items beside the hotbar when they change");
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.intOf("fade_ticks", 60, 10, 400, "How long the readout stays visible after a change")
            .bool("show_offhand", true, "Also show the offhand item")
            .intOf("x_offset", 0, -200, 200, "Horizontal offset from the default position")
            .intOf("y_offset", 0, -200, 200, "Vertical offset from the default position");
    }

    @Override
    public Supplier<ClientModule> client () {
        return () -> new HeldItemReadoutClient(this);
    }
}
