package com.chaevsfe.valence.core.config;

import java.util.Map;

public final class ConfigSnapshot
{
    private final Map<String, Boolean> enabled;
    private final Map<String, ConfigView> options;

    ConfigSnapshot (Map<String, Boolean> enabled, Map<String, ConfigView> options) {
        this.enabled = Map.copyOf(enabled);
        this.options = Map.copyOf(options);
    }

    public boolean enabled (String moduleId) {
        return enabled.getOrDefault(moduleId, false);
    }

    public ConfigView options (String moduleId) {
        return options.getOrDefault(moduleId, ConfigView.EMPTY);
    }
}
