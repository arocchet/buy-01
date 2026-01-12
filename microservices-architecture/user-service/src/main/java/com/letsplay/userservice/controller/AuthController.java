package com.letsplay.userservice.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.letsplay.userservice.dto.JwtResponse;
import com.letsplay.userservice.dto.LoginRequest;
import com.letsplay.userservice.dto.RegisterRequest;
import com.letsplay.userservice.kafka.UserEventProducer;
import com.letsplay.userservice.model.User;
import com.letsplay.userservice.security.JwtUtil;
import com.letsplay.userservice.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserEventProducer userEventProducer;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<User> userOptional = userService.getUserByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"User not found\"}");
            }

            User user = userOptional.get();
            String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

            return ResponseEntity
                    .ok(new JwtResponse(jwt, user.getId(), user.getEmail(), user.getName(), user.getRole()));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Invalid credentials\"}");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        System.out.println("DEBUG: Register request received for email: " + registerRequest.getEmail());
        try {
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : "client");

            User result = userService.createUser(user);

            // Publish user created event
            userEventProducer.sendUserCreatedEvent(result.getId(), result.getEmail(), result.getRole());

            // Auto-login after registration
            String jwt = jwtUtil.generateToken(result.getId(), result.getEmail(), result.getRole());

            return ResponseEntity
                    .ok(new JwtResponse(jwt, result.getId(), result.getEmail(), result.getName(), result.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
