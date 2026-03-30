package com.letsplay.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "very-long-test-secret-key-for-jwt-signing-1234567890-abcdef-1234567890";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
    }

    @Test
    void generateToken_WithUserDetails_ShouldValidateAndExtractUsername() {
        UserDetails userDetails = User.withUsername("alice@example.com")
                .password("encoded-password")
                .authorities("ROLE_CLIENT")
                .build();

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("alice@example.com", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void generateToken_WithClaims_ShouldExtractUserIdAndRole() {
        String token = jwtUtil.generateToken("user-123", "alice@example.com", "client");

        assertEquals("user-123", jwtUtil.extractUserId(token));
        assertEquals("client", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalseForMalformedToken() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
    }

    @Test
    void validateToken_WithUserDetails_ShouldReturnFalseWhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken("alice@example.com", "alice@example.com", "client");
        UserDetails otherUser = User.withUsername("bob@example.com")
                .password("encoded-password")
                .authorities("ROLE_CLIENT")
                .build();

        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken("user-123", "alice@example.com", "client");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }
}
