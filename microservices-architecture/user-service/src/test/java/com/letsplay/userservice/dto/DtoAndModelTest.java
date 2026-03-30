package com.letsplay.userservice.dto;

import com.letsplay.userservice.exception.BadRequestException;
import com.letsplay.userservice.exception.ErrorResponse;
import com.letsplay.userservice.exception.ResourceNotFoundException;
import com.letsplay.userservice.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoAndModelTest {

    @Test
    void loginRequest_ShouldSupportConstructorsAndAccessors() {
        LoginRequest empty = new LoginRequest();
        empty.setEmail("alice@example.com");
        empty.setPassword("pwd");
        assertEquals("alice@example.com", empty.getEmail());
        assertEquals("pwd", empty.getPassword());

        LoginRequest full = new LoginRequest("bob@example.com", "secret");
        assertEquals("bob@example.com", full.getEmail());
        assertEquals("secret", full.getPassword());
    }

    @Test
    void registerRequest_ShouldSupportConstructorsAndAccessors() {
        RegisterRequest empty = new RegisterRequest();
        empty.setName("Alice");
        empty.setEmail("alice@example.com");
        empty.setPassword("Password123");
        empty.setRole("client");

        assertEquals("Alice", empty.getName());
        assertEquals("alice@example.com", empty.getEmail());
        assertEquals("Password123", empty.getPassword());
        assertEquals("client", empty.getRole());

        RegisterRequest full = new RegisterRequest("Bob", "bob@example.com", "Password123", "seller");
        assertEquals("Bob", full.getName());
        assertEquals("seller", full.getRole());
    }

    @Test
    void jwtResponse_ShouldSupportAccessors() {
        JwtResponse response = new JwtResponse("token", "u1", "alice@example.com", "Alice", "client");
        assertEquals("token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("u1", response.getId());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("Alice", response.getName());
        assertEquals("client", response.getRole());

        response.setToken("new-token");
        response.setType("Custom");
        response.setId("u2");
        response.setEmail("bob@example.com");
        response.setName("Bob");
        response.setRole("seller");

        assertEquals("new-token", response.getToken());
        assertEquals("Custom", response.getType());
        assertEquals("u2", response.getId());
        assertEquals("bob@example.com", response.getEmail());
        assertEquals("Bob", response.getName());
        assertEquals("seller", response.getRole());
    }

    @Test
    void user_ShouldSupportConstructorsAndAccessors() {
        User empty = new User();
        empty.setId("u1");
        empty.setName("Alice");
        empty.setEmail("alice@example.com");
        empty.setPassword("pwd");
        empty.setRole("client");
        empty.setAvatar("avatar.png");

        assertEquals("u1", empty.getId());
        assertEquals("Alice", empty.getName());
        assertEquals("alice@example.com", empty.getEmail());
        assertEquals("pwd", empty.getPassword());
        assertEquals("client", empty.getRole());
        assertEquals("avatar.png", empty.getAvatar());

        User noAvatar = new User("Bob", "bob@example.com", "pwd", "seller");
        assertEquals("Bob", noAvatar.getName());
        assertEquals("seller", noAvatar.getRole());

        User withAvatar = new User("Carl", "carl@example.com", "pwd", "seller", "pic.webp");
        assertEquals("pic.webp", withAvatar.getAvatar());
    }

    @Test
    void errorResponse_ShouldSupportConstructorsAndAccessors() {
        ErrorResponse empty = new ErrorResponse();
        assertNotNull(empty.getTimestamp());

        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid", "/api/test");
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Invalid", response.getMessage());
        assertEquals("/api/test", response.getPath());

        LocalDateTime now = LocalDateTime.now();
        response.setTimestamp(now);
        response.setStatus(401);
        response.setError("Unauthorized");
        response.setMessage("Bad credentials");
        response.setPath("/api/auth");
        response.setValidationErrors(List.of("email: invalid"));

        assertEquals(now, response.getTimestamp());
        assertEquals(401, response.getStatus());
        assertEquals("Unauthorized", response.getError());
        assertEquals("Bad credentials", response.getMessage());
        assertEquals("/api/auth", response.getPath());
        assertEquals(1, response.getValidationErrors().size());
    }

    @Test
    void customExceptions_ShouldPreserveMessage() {
        BadRequestException badRequest = new BadRequestException("Invalid input");
        ResourceNotFoundException notFound = new ResourceNotFoundException("User missing");

        assertEquals("Invalid input", badRequest.getMessage());
        assertEquals("User missing", notFound.getMessage());
    }
}
