package com.letsplay.userservice.security;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    private static final Pattern MONGO_INJECTION_PATTERN = Pattern.compile(
        "\\$|\\{|\\}|\\bjavascript\\b|\\beval\\b|\\bwhere\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HTML_SCRIPT_PATTERN = Pattern.compile(
        "<\\s*/?\\s*script\\b[^>]*>|javascript:|on\\w+\\s*=",
        Pattern.CASE_INSENSITIVE
    );

    public boolean isValidInput(String input) {
        if (input == null) {
            return true;
        }

        return !MONGO_INJECTION_PATTERN.matcher(input).find()
            && !HTML_SCRIPT_PATTERN.matcher(input).find();
    }

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input.replaceAll("[<>\"'&]", "");
        sanitized = sanitized.replaceAll("\\$", "");
        sanitized = sanitized.replaceAll("\\{|\\}", "");

        return sanitized.trim();
    }

    public void validateUserInput(String... inputs) {
        for (String input : inputs) {
            if (!isValidInput(input)) {
                throw new SecurityException("Invalid input detected. Potential security threat.");
            }
        }
    }
}
