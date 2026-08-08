package com.chaevsfe.valence.core.module;

import com.chaevsfe.valence.core.config.ConfigSchema;
import com.chaevsfe.valence.core.config.ConfigSnapshot;
import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.modules.helditem.HeldItemReadout;
import com.chaevsfe.valence.modules.infohud.InfoHud;
import com.chaevsfe.valence.modules.inventoryactions.InventoryActions;
import com.chaevsfe.valence.modules.reacharound.Reacharound;
import com.chaevsfe.valence.modules.satchel.SeedSatchel;
import com.chaevsfe.valence.modules.trough.AnimalTrough;
import com.chaevsfe.valence.modules.verticalslabs.VerticalSlabs;
import com.chaevsfe.valence.modules.woodworks.Woodworks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Modules
{
    public static final List<ValenceModule> ALL = List.of(
        new HeldItemReadout(),
        new InfoHud(),
        new VerticalSlabs(),
        new Woodworks(),
        new AnimalTrough(),
        new SeedSatchel(),
        new InventoryActions(),
        new Reacharound());

    private static final Map<String, ValenceModule> BY_ID =
        ALL.stream().collect(Collectors.toMap(m -> m.id, Function.identity()));

    private Modules () { }

    public static boolean isEnabled (String id) {
        ValenceModule m = BY_ID.get(id);
        return m != null && m.enabled();
    }

    public static ValenceModule get (String id) {
        return BY_ID.get(id);
    }

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
