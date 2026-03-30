package com.letsplay.userservice.controller;

import com.letsplay.userservice.dto.JwtResponse;
import com.letsplay.userservice.dto.LoginRequest;
import com.letsplay.userservice.dto.RegisterRequest;
import com.letsplay.userservice.kafka.UserEventProducer;
import com.letsplay.userservice.model.User;
import com.letsplay.userservice.security.JwtUtil;
import com.letsplay.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController();
        ReflectionTestUtils.setField(authController, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(authController, "userService", userService);
        ReflectionTestUtils.setField(authController, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(authController, "userEventProducer", userEventProducer);
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponseOnSuccess() {
        LoginRequest request = new LoginRequest("alice@example.com", "secret");
        User user = new User("Alice", "alice@example.com", "encoded", "client");
        user.setId("u1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("u1", "alice@example.com", "client")).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(JwtResponse.class, response.getBody());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("jwt-token", jwtResponse.getToken());
    }

    @Test
    void authenticateUser_ShouldReturnBadRequestWhenUserNotFound() {
        LoginRequest request = new LoginRequest("missing@example.com", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"message\": \"User not found\"}", response.getBody());
    }

    @Test
    void authenticateUser_ShouldReturnBadRequestOnAuthenticationException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid"));

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"message\": \"Invalid credentials\"}", response.getBody());
    }

    @Test
    void registerUser_ShouldCreatePublishAndReturnJwtResponse() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "Password123", null);
        User created = new User("Alice", "alice@example.com", "encoded", "client");
        created.setId("u1");

        when(userService.createUser(any(User.class))).thenReturn(created);
        when(jwtUtil.generateToken("u1", "alice@example.com", "client")).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(JwtResponse.class, response.getBody());
        verify(userEventProducer).sendUserCreatedEvent("u1", "alice@example.com", "client");
    }

    @Test
    void registerUser_ShouldReturnBadRequestOnRuntimeException() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "Password123", "client");
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Email already exists"));

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("{\"message\": \"Email already exists\"}", response.getBody());
    }
}
