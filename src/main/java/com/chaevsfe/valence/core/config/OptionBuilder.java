package com.chaevsfe.valence.core.config;

import java.util.ArrayList;
import java.util.List;

public final class OptionBuilder
{
    private final List<Option> options = new ArrayList<>();

    public OptionBuilder bool (String key, boolean def, String comment) {
        options.add(Option.bool(key, def, comment));
        return this;
    }

    public OptionBuilder intOf (String key, int def, int min, int max, String comment) {
        options.add(Option.intOf(key, def, min, max, comment));
        return this;
    }

    public OptionBuilder doubleOf (String key, double def, double min, double max, String comment) {
        options.add(Option.doubleOf(key, def, min, max, comment));
        return this;
    }

    public OptionBuilder string (String key, String def, String comment) {
        options.add(Option.string(key, def, comment));
        return this;
    }

    public OptionBuilder strings (String key, List<String> def, String comment) {
        options.add(Option.strings(key, def, comment));
        return this;
    }

    public List<Option> build () {
        return List.copyOf(options);
    }
}
