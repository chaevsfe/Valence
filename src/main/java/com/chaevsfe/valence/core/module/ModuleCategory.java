package com.chaevsfe.valence.core.module;

import java.util.Locale;

public enum ModuleCategory
{
    BUILDING,
    UTILITY,
    INTERFACE,
    PLACEMENT;

    public String key () {
        return name().toLowerCase(Locale.ROOT);
    }
}
