package org.tavall.database.testing;

import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.util.Optional;
import java.util.OptionalInt;

final class RemoteDatabaseEnvironment {

    private RemoteDatabaseEnvironment() {
    }

    static Optional<String> firstValue(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    static String requiredValue(String description, String... names) {
        Optional<String> valueOptional = firstValue(names);
        Assumptions.assumeTrue(
                valueOptional.isPresent(),
                description + " is not configured."
        );
        return valueOptional.get();
    }

    static OptionalInt firstInt(String... names) {
        Optional<String> valueOptional = firstValue(names);
        if (valueOptional.isEmpty()) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(Integer.parseInt(valueOptional.get()));
    }

    static int intOrDefault(int defaultValue, String... names) {
        OptionalInt valueOptional = firstInt(names);
        if (valueOptional.isEmpty()) {
            return defaultValue;
        }
        return valueOptional.getAsInt();
    }

    static boolean booleanOrDefault(boolean defaultValue, String... names) {
        Optional<String> valueOptional = firstValue(names);
        if (valueOptional.isEmpty()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(valueOptional.get());
    }

    static URI requiredUri(String description, String... names) {
        String value = requiredValue(description, names);
        return URI.create(value);
    }
}

