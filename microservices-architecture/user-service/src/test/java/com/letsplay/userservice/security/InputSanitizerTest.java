package com.letsplay.userservice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputSanitizerTest {

    private final InputSanitizer inputSanitizer = new InputSanitizer();

    @Test
    void isValidInput_ShouldReturnTrueForNullInput() {
        assertTrue(inputSanitizer.isValidInput(null));
    }

    @Test
    void isValidInput_ShouldReturnTrueForSafeInput() {
        assertTrue(inputSanitizer.isValidInput("John Doe"));
    }

    @Test
    void isValidInput_ShouldReturnFalseForMongoToken() {
        assertFalse(inputSanitizer.isValidInput("$ne:admin"));
    }

    @Test
    void isValidInput_ShouldReturnFalseForHtmlScriptToken() {
        assertFalse(inputSanitizer.isValidInput("<script>alert(1)</script>"));
    }

    @Test
    void isValidInput_ShouldReturnFalseForInlineEventHandler() {
        assertFalse(inputSanitizer.isValidInput("<img src=x onerror = alert(1) />"));
    }

    @Test
    void sanitize_ShouldReturnNullForNullInput() {
        assertNull(inputSanitizer.sanitize(null));
    }

    @Test
    void sanitize_ShouldStripDangerousCharactersAndTrim() {
        String sanitized = inputSanitizer.sanitize("  <b>$John{Doe}</b>'\"&  ");
        assertEquals("bJohnDoe/b", sanitized);
    }

    @Test
    void validateUserInput_ShouldThrowForInvalidInput() {
        assertThrows(SecurityException.class,
                () -> inputSanitizer.validateUserInput("safe", "javascript:alert(1)"));
    }

    @Test
    void validateUserInput_ShouldNotThrowForSafeInputs() {
        assertDoesNotThrow(() -> inputSanitizer.validateUserInput("Alice", "alice@example.com", "client"));
    }
}
