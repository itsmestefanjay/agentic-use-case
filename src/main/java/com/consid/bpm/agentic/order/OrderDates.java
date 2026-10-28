package com.consid.bpm.agentic.order;

import com.consid.bpm.agentic.common.Inputs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/// Parses order dates in the formats customers typically use: ISO, German, Swedish and English,
/// ISO timestamps, and the words "today" and "yesterday".
final class OrderDates {

    private static final List<DateTimeFormatter> FORMATS = Stream.of(
                    "uuuu-M-d", "d.M.uuuu", "d/M/uuuu", "uuuu/M/d", "d MMM uuuu", "d MMMM uuuu", "MMM d uuuu", "MMMM d uuuu")
            .map(pattern -> new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern(pattern)
                    .toFormatter(Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT))
            .toList();

    private OrderDates() {
    }

    static Optional<LocalDate> parse(String raw, LocalDate today) {
        if (Inputs.isMissing(raw)) {
            return Optional.empty();
        }
        String value = raw.strip().replace(",", "").replaceAll("\\s+", " ");
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "today" -> Optional.of(today);
            case "yesterday" -> Optional.of(today.minusDays(1));
            default -> parseFormatted(value.matches("\\d{4}-\\d{2}-\\d{2}T.*") ? value.substring(0, 10) : value);
        };
    }

    private static Optional<LocalDate> parseFormatted(String value) {
        return FORMATS.stream()
                .flatMap(format -> tryParse(value, format).stream())
                .findFirst();
    }

    private static Optional<LocalDate> tryParse(String value, DateTimeFormatter format) {
        try {
            return Optional.of(LocalDate.parse(value, format));
        } catch (DateTimeParseException _) {
            return Optional.empty();
        }
    }
}
