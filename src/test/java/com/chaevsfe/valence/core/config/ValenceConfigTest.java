package com.chaevsfe.valence.core.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ValenceConfigTest
{
    @TempDir
    Path dir;

    private static ConfigSchema schema () {
        return new ConfigSchema(
            List.of("building", "utility"),
            List.of(
                new ConfigSchema.ModuleDef("vertical_slabs", "building", "Vertical slab variants", true, List.of()),
                new ConfigSchema.ModuleDef("animal_trough", "utility", "Feeds nearby animals", true, List.of(
                    Option.intOf("range", 8, 1, 16, "Scan radius in blocks"),
                    Option.bool("suppress_xp", true, "Trough-bred animals drop no XP")))));
    }

    private Path file () {
        return dir.resolve("valence.json5");
    }

    @Test
    void freshFileWritesDefaults () throws Exception {
        ConfigSnapshot snap = ValenceConfig.load(file(), schema());
        assertTrue(snap.enabled("vertical_slabs"));
        assertEquals(8, snap.options("animal_trough").intOf("range"));
        assertTrue(Files.exists(file()));
        String text = Files.readString(file());
        assertTrue(text.contains("// Scan radius in blocks [1..16]"));
        assertTrue(text.contains("\"vertical_slabs\": true"));
    }

    @Test
    void userValuesSurvive () throws Exception {
        Files.writeString(file(), """
            {
              modules: { utility: { animal_trough: false } },
              options: { animal_trough: { range: 12 } },
            }
            """);
        ConfigSnapshot snap = ValenceConfig.load(file(), schema());
        assertFalse(snap.enabled("animal_trough"));
        assertEquals(12, snap.options("animal_trough").intOf("range"));

        ConfigSnapshot again = ValenceConfig.load(file(), schema());
        assertFalse(again.enabled("animal_trough"));
        assertEquals(12, again.options("animal_trough").intOf("range"));
    }

    @Test
    void missingKeysGetDefaults () throws Exception {
        Files.writeString(file(), "{ modules: { building: { vertical_slabs: false } } }");
        ValenceConfig.load(file(), schema());
        String text = Files.readString(file());
        assertTrue(text.contains("\"animal_trough\": true"));
        assertTrue(text.contains("\"range\": 8"));
        assertTrue(text.contains("\"vertical_slabs\": false"));
    }

    @Test
    void unknownKeysPreserved () throws Exception {
        Files.writeString(file(), """
            {
              modules: { utility: { future_module: true } },
              options: { animal_trough: { custom: "x" } },
              extra: { a: [1, 2] },
            }
            """);
        ValenceConfig.load(file(), schema());
        String text = Files.readString(file());
        assertTrue(text.contains("\"future_module\": true"));
        assertTrue(text.contains("\"custom\": \"x\""));
        assertTrue(text.contains("\"a\": [1, 2]"));
    }

    @Test
    void outOfRangeClamped () throws Exception {
        Files.writeString(file(), "{ options: { animal_trough: { range: 99 } } }");
        ConfigSnapshot snap = ValenceConfig.load(file(), schema());
        assertEquals(16, snap.options("animal_trough").intOf("range"));
    }

    @Test
    void wrongTypeFallsBackToDefault () throws Exception {
        Files.writeString(file(), "{ options: { animal_trough: { range: \"loads\", suppress_xp: 3 } } }");
        ConfigSnapshot snap = ValenceConfig.load(file(), schema());
        assertEquals(8, snap.options("animal_trough").intOf("range"));
        assertTrue(snap.options("animal_trough").bool("suppress_xp"));
    }

    @Test
    void brokenFilePreservedNotDeleted () throws Exception {
        Files.writeString(file(), "{ not json !!");
        ConfigSnapshot snap = ValenceConfig.load(file(), schema());
        assertTrue(snap.enabled("animal_trough"));
        assertTrue(Files.exists(dir.resolve("valence.json5.broken")));
        assertTrue(Files.readString(dir.resolve("valence.json5.broken")).contains("not json"));
    }

    @Test
    void roundTripStable () throws Exception {
        ValenceConfig.load(file(), schema());
        String first = Files.readString(file());
        ValenceConfig.load(file(), schema());
        assertEquals(first, Files.readString(file()));
    }

    @Test
    void saveWritesClampsAndReloads () throws Exception {
        ValenceConfig.load(file(), schema());
        ConfigSnapshot saved = ValenceConfig.save(file(), schema(),
            Map.of("vertical_slabs", false, "animal_trough", true),
            Map.of("animal_trough", Map.of("range", 99, "suppress_xp", false)));
        assertFalse(saved.enabled("vertical_slabs"));
        assertEquals(16, saved.options("animal_trough").intOf("range"));

        ConfigSnapshot loaded = ValenceConfig.load(file(), schema());
        assertFalse(loaded.enabled("vertical_slabs"));
        assertEquals(16, loaded.options("animal_trough").intOf("range"));
        assertFalse(loaded.options("animal_trough").bool("suppress_xp"));
    }
}
