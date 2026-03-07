package com.hsbc.ci.engine.core.cli;

import java.util.Map;
import java.util.stream.Collectors;

public class JsonOutput {

    public static String toJson(Object obj) {
        if (obj instanceof Map) {
            return toJson((Map<String, Object>) obj);
        }
        return "{}";
    }

    public static String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        String entries = map.entrySet().stream()
            .map(e -> "  \"" + escape(e.getKey()) + "\": " + formatValue(e.getValue()))
            .collect(Collectors.joining(",\n"));
        
        sb.append(entries);
        sb.append("\n}");
        
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Map) {
            return toJson((Map<String, Object>) value);
        } else if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) sb.append(", ");
                sb.append(formatValue(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }
}
