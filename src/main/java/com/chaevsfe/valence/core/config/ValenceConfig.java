package com.chaevsfe.valence.core.config;

import com.chaevsfe.valence.core.ModServices;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ValenceConfig
{
    private ValenceConfig () { }

    public static ConfigSnapshot load (Path file, ConfigSchema schema) {
        Map<String, Object> parsed = readTree(file);
        ConfigSnapshot snap = snapshot(schema, parsed);
        String emitted = ConfigEmitter.emit(schema, snap, parsed);
        writeIfChanged(file, emitted);
        return snap;
    }

    public static ConfigSnapshot save (Path file, ConfigSchema schema, Map<String, Boolean> enabled, Map<String, Map<String, Object>> values) {
        Map<String, Object> parsed = readTree(file);
        Map<String, Boolean> outEnabled = new LinkedHashMap<>();
        Map<String, ConfigView> outOptions = new LinkedHashMap<>();
        for (ConfigSchema.ModuleDef def : schema.modules) {
            Boolean on = enabled.get(def.id());
            outEnabled.put(def.id(), on != null ? on : def.defaultEnabled());
            Map<String, Object> in = values.getOrDefault(def.id(), Map.of());
            Map<String, Object> sane = new LinkedHashMap<>();
            for (Option o : def.options())
                sane.put(o.key, o.sanitize(in.get(o.key)));
            outOptions.put(def.id(), new ConfigView(sane));
        }
        ConfigSnapshot snap = new ConfigSnapshot(outEnabled, outOptions);
        writeIfChanged(file, ConfigEmitter.emit(schema, snap, parsed));
        return snap;
    }

    private static Map<String, Object> readTree (Path file) {
        if (!Files.exists(file))
            return Map.of();
        try {
            String text = Files.readString(file);
            if (JsonParser.parse(text) instanceof Map<?, ?> map) {
                Map<String, Object> tree = new LinkedHashMap<>();
                map.forEach((k, v) -> tree.put(String.valueOf(k), v));
                return tree;
            }
            throw new IllegalArgumentException("root is not an object");
        }
        catch (Exception e) {
            Path broken = file.resolveSibling(file.getFileName() + ".broken");
            try {
                Files.copy(file, broken, StandardCopyOption.REPLACE_EXISTING);
                ModServices.LOG.warn("Unreadable config kept as {}: {}", broken.getFileName(), e.getMessage());
            }
            catch (IOException io) {
                ModServices.reportOnce("config-preserve", io);
            }
            return Map.of();
        }
    }

    private static ConfigSnapshot snapshot (ConfigSchema schema, Map<String, Object> parsed) {
        Map<String, Object> modulesTree = ConfigEmitter.child(parsed, "modules");
        Map<String, Object> optionsTree = ConfigEmitter.child(parsed, "options");

        Map<String, Boolean> enabled = new LinkedHashMap<>();
        Map<String, ConfigView> options = new LinkedHashMap<>();
        for (ConfigSchema.ModuleDef def : schema.modules) {
            Object raw = ConfigEmitter.child(modulesTree, def.category()).get(def.id());
            enabled.put(def.id(), raw instanceof Boolean b ? b : def.defaultEnabled());

            Map<String, Object> optTree = ConfigEmitter.child(optionsTree, def.id());
            Map<String, Object> values = new LinkedHashMap<>();
            for (Option o : def.options())
                values.put(o.key, o.sanitize(optTree.get(o.key)));
            options.put(def.id(), new ConfigView(values));
        }
        return new ConfigSnapshot(enabled, options);
    }

    private static void writeIfChanged (Path file, String content) {
        try {
            if (Files.exists(file) && Files.readString(file).equals(content))
                return;
            if (file.getParent() != null)
                Files.createDirectories(file.getParent());
            Files.writeString(file, content);
        }
        catch (IOException e) {
            ModServices.reportOnce("config-write", e);
        }
    }
}
