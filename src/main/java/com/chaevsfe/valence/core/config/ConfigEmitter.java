package com.chaevsfe.valence.core.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class ConfigEmitter
{
    private ConfigEmitter () { }

    static String emit (ConfigSchema schema, ConfigSnapshot snap, Map<String, Object> parsed) {
        Map<String, Object> modulesTree = child(parsed, "modules");
        Map<String, Object> optionsTree = child(parsed, "options");

        List<String> top = new ArrayList<>();
        top.add(entry(List.of(), "modules", modulesObj(schema, snap, modulesTree, "  "), "  "));

        String options = optionsObj(schema, snap, optionsTree, "  ");
        if (!options.equals("{ }"))
            top.add(entry(List.of(), "options", options, "  "));

        for (Map.Entry<String, Object> e : parsed.entrySet())
            if (!e.getKey().equals("modules") && !e.getKey().equals("options"))
                top.add(entry(List.of(), e.getKey(), value(e.getValue(), "  "), "  "));

        return obj(top, "") + "\n";
    }

    private static String modulesObj (ConfigSchema schema, ConfigSnapshot snap, Map<String, Object> modulesTree, String indent) {
        String inner = indent + "  ";
        List<String> cats = new ArrayList<>();
        Set<String> knownCats = schema.categories.stream().collect(Collectors.toSet());

        for (String cat : schema.categories) {
            List<ConfigSchema.ModuleDef> defs = schema.inCategory(cat);
            Map<String, Object> catTree = child(modulesTree, cat);
            if (defs.isEmpty() && catTree.isEmpty())
                continue;

            List<String> entries = new ArrayList<>();
            Set<String> knownIds = defs.stream().map(ConfigSchema.ModuleDef::id).collect(Collectors.toSet());
            for (ConfigSchema.ModuleDef def : defs)
                entries.add(entry(List.of(def.description()), def.id(), String.valueOf(snap.enabled(def.id())), inner + "  "));
            for (Map.Entry<String, Object> e : catTree.entrySet())
                if (!knownIds.contains(e.getKey()))
                    entries.add(entry(List.of(), e.getKey(), value(e.getValue(), inner + "  "), inner + "  "));

            cats.add(entry(List.of(), cat, obj(entries, inner), inner));
        }

        for (Map.Entry<String, Object> e : modulesTree.entrySet())
            if (!knownCats.contains(e.getKey()))
                cats.add(entry(List.of(), e.getKey(), value(e.getValue(), inner), inner));

        return obj(cats, indent);
    }

    private static String optionsObj (ConfigSchema schema, ConfigSnapshot snap, Map<String, Object> optionsTree, String indent) {
        String inner = indent + "  ";
        List<String> blocks = new ArrayList<>();
        Set<String> knownIds = schema.modules.stream().map(ConfigSchema.ModuleDef::id).collect(Collectors.toSet());

        for (ConfigSchema.ModuleDef def : schema.modules) {
            Map<String, Object> optTree = child(optionsTree, def.id());
            if (def.options().isEmpty() && optTree.isEmpty())
                continue;

            List<String> entries = new ArrayList<>();
            Set<String> knownKeys = def.options().stream().map(o -> o.key).collect(Collectors.toSet());
            Map<String, Object> values = snap.options(def.id()).raw();
            for (Option o : def.options())
                entries.add(entry(List.of(o.comment + o.rangeHint()), o.key, value(values.get(o.key), inner + "  "), inner + "  "));
            for (Map.Entry<String, Object> e : optTree.entrySet())
                if (!knownKeys.contains(e.getKey()))
                    entries.add(entry(List.of(), e.getKey(), value(e.getValue(), inner + "  "), inner + "  "));

            blocks.add(entry(List.of(), def.id(), obj(entries, inner), inner));
        }

        for (Map.Entry<String, Object> e : optionsTree.entrySet())
            if (!knownIds.contains(e.getKey()))
                blocks.add(entry(List.of(), e.getKey(), value(e.getValue(), inner), inner));

        return obj(blocks, indent);
    }

    private static String entry (List<String> comments, String key, String rendered, String indent) {
        StringBuilder sb = new StringBuilder();
        for (String comment : comments)
            if (!comment.isBlank())
                sb.append(indent).append("// ").append(comment).append('\n');
        sb.append(indent).append('"').append(key).append("\": ").append(rendered);
        return sb.toString();
    }

    private static String obj (List<String> entries, String indent) {
        if (entries.isEmpty())
            return "{ }";
        return "{\n" + String.join(",\n", entries) + "\n" + indent + "}";
    }

    private static String value (Object v, String indent) {
        if (v == null)
            return "null";
        if (v instanceof String s)
            return quote(s);
        if (v instanceof Map<?, ?> map) {
            List<String> entries = new ArrayList<>();
            for (Map.Entry<?, ?> e : map.entrySet())
                entries.add(entry(List.of(), String.valueOf(e.getKey()), value(e.getValue(), indent + "  "), indent + "  "));
            return obj(entries, indent);
        }
        if (v instanceof List<?> list)
            return "[" + list.stream().map(e -> value(e, indent)).collect(Collectors.joining(", ")) + "]";
        return String.valueOf(v);
    }

    private static String quote (String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\r' -> sb.append("\\r");
                default -> {
                    if (c < 0x20)
                        sb.append(String.format("\\u%04x", (int) c));
                    else
                        sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> child (Map<String, Object> parent, String key) {
        return parent.get(key) instanceof Map<?, ?> map ? (Map<String, Object>) map : new LinkedHashMap<>();
    }
}
