package com.consid.bpm.agentic.customer;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public record Customer(String id, String name, String email, String phone, ContactChannel preferredChannel,
                       String language, CustomerTier tier) {

    /// The email address or phone number, depending on the preferred channel.
    public String preferredAddress() {
        return switch (preferredChannel) {
            case EMAIL -> email;
            case SMS -> phone;
        };
    }

    /// Every part of the given name has to appear in the registered name, ignoring case and accents,
    /// so `"anna"` and `"Berg, Anna"` both match `"Anna Berg"`.
    public boolean hasName(String givenName) {
        return nameParts(name).containsAll(nameParts(givenName));
    }

    private static List<String> nameParts(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return Arrays.stream(withoutAccents.toLowerCase(Locale.ROOT).split("[^a-z]+"))
                .filter(part -> !part.isEmpty())
                .toList();
    }
}
