package com.letsplay.userservice.security;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    private static final String[] MONGO_TOKENS = {
            "$", "{", "}", "javascript", "eval", "where"
    };

    private static final String[] HTML_TOKENS = {
            "<script", "</script", "javascript:"
    };

    public boolean isValidInput(String input) {
        if (input == null) {
            return true;
        }

        String normalizedInput = input.toLowerCase(Locale.ROOT);
        return !containsAnyToken(normalizedInput, MONGO_TOKENS)
                && !containsAnyToken(normalizedInput, HTML_TOKENS)
                && !hasInlineEventHandler(normalizedInput);
    }

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input
                .replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("&", "")
                .replace("$", "")
                .replace("{", "")
                .replace("}", "");

        return sanitized.trim();
    }

    private boolean containsAnyToken(String input, String[] tokens) {
        for (String token : tokens) {
            if (input.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInlineEventHandler(String input) {
        int length = input.length();
        for (int i = 0; i < length - 2; i++) {
            if (input.charAt(i) != 'o' || input.charAt(i + 1) != 'n') {
                continue;
            }

            int j = i + 2;
            boolean hasAttributeName = false;
            while (j < length && Character.isLetter(input.charAt(j))) {
                hasAttributeName = true;
                j++;
            }

            while (j < length && Character.isWhitespace(input.charAt(j))) {
                j++;
            }

            if (hasAttributeName && j < length && input.charAt(j) == '=') {
                return true;
            }
        }
        return false;
    }

    public void validateUserInput(String... inputs) {
        for (String input : inputs) {
            if (!isValidInput(input)) {
                throw new SecurityException("Invalid input detected. Potential security threat.");
            }
        }
    }
}
