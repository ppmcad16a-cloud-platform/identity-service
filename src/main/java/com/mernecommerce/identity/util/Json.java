package com.mernecommerce.identity.util;

import java.util.LinkedHashMap;
import java.util.Map;

// Tiny response-body builder used across every controller instead of
// Map.of(...) — Map.of throws on a null value, and several original fields
// (firstName, avatar, etc.) can legitimately be null.
public final class Json {
    public static Map<String, Object> of(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private Json() {}
}
