package com.letsplay.userservice.security;

import com.letsplay.userservice.model.User;
import com.letsplay.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customUserDetailsService = new CustomUserDetailsService();
        ReflectionTestUtils.setField(customUserDetailsService, "userRepository", userRepository);
    }

    @Test
    void loadUserByUsername_ShouldReturnSpringUserDetails() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("encoded-password");
        user.setRole("seller");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice@example.com");

        assertEquals("alice@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertEquals("ROLE_SELLER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_ShouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));

        assertEquals("User not found with email: missing@example.com", ex.getMessage());
    }
}
