package com.chaevsfe.valence.core.config;

import java.util.List;
import java.util.Map;

public final class ConfigView
{
    public static final ConfigView EMPTY = new ConfigView(Map.of());

    private final Map<String, Object> values;

    ConfigView (Map<String, Object> values) {
        this.values = Map.copyOf(values);
    }

    public boolean bool (String key) {
        return (Boolean) get(key);
    }

    public int intOf (String key) {
        return (Integer) get(key);
    }

    public double doubleOf (String key) {
        return (Double) get(key);
    }

    public String string (String key) {
        return (String) get(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> strings (String key) {
        return (List<String>) get(key);
    }

    private Object get (String key) {
        Object value = values.get(key);
        if (value == null)
            throw new IllegalArgumentException("unknown option '" + key + "'");
        return value;
    }

    Map<String, Object> raw () {
        return values;
    }
}
