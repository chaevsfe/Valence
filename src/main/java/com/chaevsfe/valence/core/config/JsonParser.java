package com.chaevsfe.valence.core.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser
{
    private final String src;
    private int pos;

    private JsonParser (String src) {
        this.src = src;
    }

    public static Object parse (String text) {
        JsonParser p = new JsonParser(text);
        p.skipGap();
        Object value = p.readValue();
        p.skipGap();
        if (p.pos < p.src.length())
            throw p.fail("trailing content");
        return value;
    }

    private Object readValue () {
        char c = peek();
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readQuoted();
            default -> readWord();
        };
    }

    private Map<String, Object> readObject () {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipGap();
        while (peek() != '}') {
            String key = peek() == '"' ? readQuoted() : readBareKey();
            skipGap();
            expect(':');
            skipGap();
            map.put(key, readValue());
            skipGap();
            if (peek() == ',') {
                pos++;
                skipGap();
            }
            else if (peek() != '}')
                throw fail("expected ',' or '}'");
        }
        pos++;
        return map;
    }

    private List<Object> readArray () {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipGap();
        while (peek() != ']') {
            list.add(readValue());
            skipGap();
            if (peek() == ',') {
                pos++;
                skipGap();
            }
            else if (peek() != ']')
                throw fail("expected ',' or ']'");
        }
        pos++;
        return list;
    }

    private String readQuoted () {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= src.length())
                throw fail("unterminated string");
            char c = src.charAt(pos++);
            if (c == '"')
                return sb.toString();
            if (c == '\\') {
                char e = src.charAt(pos++);
                sb.append(switch (e) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case 'b' -> '\b';
                    case 'f' -> '\f';
                    case 'u' -> {
                        char u = (char) Integer.parseInt(src.substring(pos, pos + 4), 16);
                        pos += 4;
                        yield u;
                    }
                    default -> e;
                });
            }
            else
                sb.append(c);
        }
    }

    private String readBareKey () {
        int start = pos;
        while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_' || src.charAt(pos) == '-' || src.charAt(pos) == '.'))
            pos++;
        if (pos == start)
            throw fail("expected key");
        return src.substring(start, pos);
    }

    private Object readWord () {
        int start = pos;
        while (pos < src.length() && "{}[],:\"/ \t\r\n".indexOf(src.charAt(pos)) < 0)
            pos++;
        String word = src.substring(start, pos);
        if (word.isEmpty())
            throw fail("expected value");
        return switch (word) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            case "null" -> null;
            default -> parseNumber(word);
        };
    }

    private Object parseNumber (String word) {
        try {
            if (word.indexOf('.') < 0 && word.indexOf('e') < 0 && word.indexOf('E') < 0)
                return Long.parseLong(word);
            return Double.parseDouble(word);
        }
        catch (NumberFormatException e) {
            throw fail("bad value '" + word + "'");
        }
    }

    private void skipGap () {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isWhitespace(c))
                pos++;
            else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                while (pos < src.length() && src.charAt(pos) != '\n')
                    pos++;
            }
            else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '*') {
                int end = src.indexOf("*/", pos + 2);
                if (end < 0)
                    throw fail("unterminated comment");
                pos = end + 2;
            }
            else
                return;
        }
    }

    private char peek () {
        if (pos >= src.length())
            throw fail("unexpected end");
        return src.charAt(pos);
    }

    private void expect (char c) {
        if (peek() != c)
            throw fail("expected '" + c + "'");
        pos++;
    }

    private IllegalArgumentException fail (String message) {
        int line = 1;
        for (int i = 0; i < Math.min(pos, src.length()); i++)
            if (src.charAt(i) == '\n')
                line++;
        return new IllegalArgumentException("line " + line + ": " + message);
    }
}
