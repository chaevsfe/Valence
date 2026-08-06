package com.chaevsfe.valence.core.config;

import java.util.List;

public final class Option
{
    public enum Kind { BOOL, INT, DOUBLE, STRING, STRING_LIST }

    public final Kind kind;
    public final String key;
    public final String comment;
    public final Object def;
    public final double min;
    public final double max;

    private Option (Kind kind, String key, String comment, Object def, double min, double max) {
        this.kind = kind;
        this.key = key;
        this.comment = comment;
        this.def = def;
        this.min = min;
        this.max = max;
    }

    public static Option bool (String key, boolean def, String comment) {
        return new Option(Kind.BOOL, key, comment, def, 0, 0);
    }

    public static Option intOf (String key, int def, int min, int max, String comment) {
        return new Option(Kind.INT, key, comment, def, min, max);
    }

    public static Option doubleOf (String key, double def, double min, double max, String comment) {
        return new Option(Kind.DOUBLE, key, comment, def, min, max);
    }

    public static Option string (String key, String def, String comment) {
        return new Option(Kind.STRING, key, comment, def, 0, 0);
    }

    public static Option strings (String key, List<String> def, String comment) {
        return new Option(Kind.STRING_LIST, key, comment, List.copyOf(def), 0, 0);
    }

    public Object sanitize (Object raw) {
        return switch (kind) {
            case BOOL -> raw instanceof Boolean b ? b : def;
            case INT -> raw instanceof Number n ? (int) clamp(n.longValue()) : def;
            case DOUBLE -> raw instanceof Number n ? clamp(n.doubleValue()) : def;
            case STRING -> raw instanceof String s ? s : def;
            case STRING_LIST -> raw instanceof List<?> list && list.stream().allMatch(e -> e instanceof String)
                ? List.copyOf(list.stream().map(String.class::cast).toList()) : def;
        };
    }

    private double clamp (double value) {
        return Math.max(min, Math.min(max, value));
    }

    public String rangeHint () {
        if (kind != Kind.INT && kind != Kind.DOUBLE)
            return "";
        return kind == Kind.INT
            ? " [" + (long) min + ".." + (long) max + "]"
            : " [" + min + ".." + max + "]";
    }
}
