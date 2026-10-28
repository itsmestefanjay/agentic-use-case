package com.consid.bpm.agentic.common;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/// Helpers for values typed by customers or passed on by the AI agent, which are loosely formatted.
public final class Inputs {

    private static final Set<String> PLACEHOLDERS = Set.of("null", "none", "n/a", "na", "unknown", "undefined", "-");

    private Inputs() {
    }

    /// `true` for `null`, blank values and placeholders such as `"null"` or `"n/a"` that stand for a missing value.
    public static boolean isMissing(String value) {
        return value == null || value.isBlank() || PLACEHOLDERS.contains(value.strip().toLowerCase(Locale.ROOT));
    }

    /// Normalizes an id to `PREFIX-VALUE`, so `"ord 10002"`, `"10002"` and `"ORD-10002"` all become `"ORD-10002"`.
    public static Optional<String> normalizeId(String raw, String prefix) {
        if (isMissing(raw)) {
            return Optional.empty();
        }
        String compact = raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        String value = compact.startsWith(prefix) ? compact.substring(prefix.length()) : compact;
        return value.isEmpty() ? Optional.empty() : Optional.of(prefix + "-" + value);
    }
}
