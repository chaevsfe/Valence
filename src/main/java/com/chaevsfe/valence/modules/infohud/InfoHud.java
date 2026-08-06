package com.chaevsfe.valence.modules.infohud;

import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.infohud.client.InfoHudClient;
import java.util.function.Supplier;

public class InfoHud extends ValenceModule
{
    public InfoHud () {
        super("info_hud", ModuleCategory.INTERFACE, ModuleSide.CLIENT_ONLY,
            "Toggleable overlay with coordinates, facing, biome, light and more");
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.bool("show_coords", true, "Show player coordinates")
            .bool("show_facing", true, "Show facing direction and axis")
            .bool("show_biome", true, "Show the current biome")
            .bool("show_light", true, "Show block and sky light at the player's feet")
            .bool("show_fps", false, "Show frames per second")
            .bool("show_time", true, "Show in-game day and clock time")
            .intOf("x_offset", 0, 0, 400, "Horizontal offset from the top-left corner")
            .intOf("y_offset", 0, 0, 400, "Vertical offset from the top-left corner");
    }

    @Override
    public Supplier<ClientModule> client () {
        return () -> new InfoHudClient(this);
    }
}
