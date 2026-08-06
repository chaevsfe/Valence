package com.chaevsfe.valence.core.config;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest
{
    @Test
    void lenientSyntax () {
        String text = """
            {
              // line comment
              bare_key: 1,
              "str": "a\\nb", /* block */
              nested: {
                list: [1, 2.5, true, null,],
              },
            }
            """;
        Map<?, ?> parsed = (Map<?, ?>) JsonParser.parse(text);
        assertEquals(1L, parsed.get("bare_key"));
        assertEquals("a\nb", parsed.get("str"));
        Map<?, ?> nested = (Map<?, ?>) parsed.get("nested");
        assertEquals(List.of(1L, 2.5, Boolean.TRUE), ((List<?>) nested.get("list")).subList(0, 3));
        assertNull(((List<?>) nested.get("list")).get(3));
    }

    @Test
    void escapes () {
        assertEquals("q\"\\\tu", JsonParser.parse("\"q\\\"\\\\\\tu\""));
        assertEquals("A", JsonParser.parse("\"\\u0041\""));
    }

    @Test
    void numbers () {
        assertEquals(-42L, JsonParser.parse("-42"));
        assertEquals(1.5e3, JsonParser.parse("1.5e3"));
    }

    @Test
    void trailingContentFails () {
        assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("{} extra"));
    }

    @Test
    void unterminatedStringFails () {
        assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("\"open"));
    }

    @Test
    void badValueFails () {
        assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("{ key: nope }"));
    }
}
