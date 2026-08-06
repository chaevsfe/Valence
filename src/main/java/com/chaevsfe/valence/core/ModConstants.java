package com.chaevsfe.valence.core;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class ModConstants
{
    public static final String MOD_ID = "valence";

    private ModConstants () { }

    public static Identifier loc (String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Path configPath () {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5");
    }
}
