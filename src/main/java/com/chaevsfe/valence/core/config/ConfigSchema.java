package com.chaevsfe.valence.core.config;

import java.util.List;

public final class ConfigSchema
{
    public record ModuleDef (String id, String category, String description, boolean defaultEnabled, List<Option> options) { }

    public final List<String> categories;
    public final List<ModuleDef> modules;

    public ConfigSchema (List<String> categories, List<ModuleDef> modules) {
        this.categories = List.copyOf(categories);
        this.modules = List.copyOf(modules);
    }

    public List<ModuleDef> inCategory (String category) {
        return modules.stream().filter(m -> m.category.equals(category)).toList();
    }
}
