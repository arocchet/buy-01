package com.letsplay.userservice.service;

import com.letsplay.userservice.model.User;
import com.letsplay.userservice.repository.UserRepository;
import com.letsplay.userservice.security.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldSanitizeEncodeAndSaveWithDefaultRole() {
        User user = new User();
        user.setName("  Alice  ");
        user.setEmail("alice@example.com");
        user.setPassword("PlainPassword1");
        user.setRole("");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(inputSanitizer.sanitize("  Alice  ")).thenReturn("Alice");
        when(passwordEncoder.encode("PlainPassword1")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user);

        verify(inputSanitizer).validateUserInput("  Alice  ", "alice@example.com", "");
        verify(inputSanitizer).sanitize("  Alice  ");
        assertEquals("Alice", created.getName());
        assertEquals("encodedPassword", created.getPassword());
        assertEquals("client", created.getRole());
    }

    @Test
    void createUser_ShouldThrowWhenEmailAlreadyExists() {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setRole("client");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(user));

        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnRepositoryUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void getUserById_ShouldDelegateToRepository() {
        User user = new User();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById("u1");

        assertTrue(result.isPresent());
    }

    @Test
    void getUserByEmail_ShouldDelegateToRepository() {
        User user = new User();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("alice@example.com");

        assertTrue(result.isPresent());
    }

    @Test
    void updateUser_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser("missing", new User()));

        assertEquals("User not found with id: missing", ex.getMessage());
    }

    @Test
    void updateUser_ShouldThrowWhenNewEmailAlreadyExists() {
        User existing = new User("Alice", "alice@example.com", "encoded", "client");
        existing.setId("u1");

        User details = new User();
        details.setEmail("new@example.com");

        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser("u1", details));

        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void updateUser_ShouldApplyAllProvidedFieldsAndSave() {
        User existing = new User("Alice", "alice@example.com", "encoded", "client");
        existing.setId("u1");

        User details = new User();
        details.setName("Alice Updated");
        details.setEmail("alice.new@example.com");
        details.setPassword("newPassword");
        details.setRole("seller");
        details.setAvatar("avatar.png");

        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("alice.new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser("u1", details);

        assertEquals("Alice Updated", updated.getName());
        assertEquals("alice.new@example.com", updated.getEmail());
        assertEquals("newEncodedPassword", updated.getPassword());
        assertEquals("seller", updated.getRole());
        assertEquals("avatar.png", updated.getAvatar());
    }

    @Test
    void deleteUser_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser("missing"));

        assertEquals("User not found with id: missing", ex.getMessage());
    }

    @Test
    void deleteUser_ShouldDeleteExistingUser() {
        User existing = new User();
        existing.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));
        doNothing().when(userRepository).delete(existing);

        userService.deleteUser("u1");

        verify(userRepository).delete(existing);
    }

    @Test
    void authenticateUser_ShouldReturnTrueWhenPasswordMatches() {
        User existing = new User();
        existing.setPassword("encoded");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);

        assertTrue(userService.authenticateUser("alice@example.com", "plain"));
    }

    @Test
    void authenticateUser_ShouldReturnFalseWhenUserMissingOrPasswordMismatch() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertFalse(userService.authenticateUser("missing@example.com", "plain"));

        User existing = new User();
        existing.setPassword("encoded");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertFalse(userService.authenticateUser("alice@example.com", "wrong"));
    }
}
