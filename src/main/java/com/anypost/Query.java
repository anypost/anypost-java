package com.anypost;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** A small URL query-string builder that skips null and empty values. */
final class Query {

    private final Map<String, String> params = new LinkedHashMap<>();

    Query set(String key, String value) {
        if (value != null && !value.isEmpty()) {
            params.put(key, value);
        }
        return this;
    }

    Query set(String key, Integer value) {
        if (value != null) {
            params.put(key, String.valueOf(value));
        }
        return this;
    }

    boolean isEmpty() {
        return params.isEmpty();
    }

    /** Renders {@code key=value&...} with form-encoded keys and values, or "" when empty. */
    String encode() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (b.length() > 0) {
                b.append('&');
            }
            b.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return b.toString();
    }
}
