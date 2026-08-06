package com.chaevsfe.valence.client.gui;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.config.ConfigSchema;
import com.chaevsfe.valence.core.config.ConfigSnapshot;
import com.chaevsfe.valence.core.config.ConfigView;
import com.chaevsfe.valence.core.config.Option;
import com.chaevsfe.valence.core.config.ValenceConfig;
import com.chaevsfe.valence.core.module.Modules;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigEditState
{
    public final ConfigSchema schema = Modules.schema();
    public final Map<String, Boolean> enabled = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> values = new LinkedHashMap<>();

    public ConfigEditState () {
        for (ConfigSchema.ModuleDef def : schema.modules) {
            enabled.put(def.id(), Modules.get(def.id()).enabled());
            ConfigView view = Modules.get(def.id()).options();
            Map<String, Object> vals = new LinkedHashMap<>();
            for (Option o : def.options())
                vals.put(o.key, switch (o.kind) {
                    case BOOL -> view.bool(o.key);
                    case INT -> view.intOf(o.key);
                    case DOUBLE -> view.doubleOf(o.key);
                    case STRING -> view.string(o.key);
                    case STRING_LIST -> view.strings(o.key);
                });
            values.put(def.id(), vals);
        }
    }

    public void save () {
        ConfigSnapshot snap = ValenceConfig.save(ModConstants.configPath(), schema, enabled, values);
        Modules.applyConfig(snap);
    }
}
