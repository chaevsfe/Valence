package com.chaevsfe.valence.core.module;

import com.chaevsfe.valence.core.config.ConfigSchema;
import com.chaevsfe.valence.core.config.ConfigSnapshot;
import com.chaevsfe.valence.core.config.OptionBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Modules
{
    public static final List<ValenceModule> ALL = List.of();

    private Modules () { }

    public static ConfigSchema schema () {
        List<ConfigSchema.ModuleDef> defs = new ArrayList<>();
        for (ValenceModule m : ALL) {
            OptionBuilder builder = new OptionBuilder();
            m.defineOptions(builder);
            defs.add(new ConfigSchema.ModuleDef(m.id, m.category.key(), m.description, m.enabledByDefault(), builder.build()));
        }
        List<String> categories = Arrays.stream(ModuleCategory.values()).map(ModuleCategory::key).toList();
        return new ConfigSchema(categories, defs);
    }

    public static void applyConfig (ConfigSnapshot snap) {
        for (ValenceModule m : ALL)
            m.apply(snap);
    }
}
