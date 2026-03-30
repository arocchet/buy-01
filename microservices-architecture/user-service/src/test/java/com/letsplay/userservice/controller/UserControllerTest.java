package com.letsplay.userservice.controller;

import com.letsplay.userservice.model.User;
import com.letsplay.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test
    void getMyProfile_ShouldReturnUserWhenFound() {
        User user = new User();
        user.setId("u1");
        when(authentication.getName()).thenReturn("u1");
        when(userService.getUserById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getMyProfile(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void getMyProfile_ShouldReturnNotFoundWhenMissing() {
        when(authentication.getName()).thenReturn("u1");
        when(userService.getUserById("u1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getMyProfile(authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateMyProfile_ShouldReturnUpdatedUser() {
        User details = new User();
        User updated = new User();
        when(authentication.getName()).thenReturn("u1");
        when(userService.updateUser("u1", details)).thenReturn(updated);

        ResponseEntity<?> response = userController.updateMyProfile(details, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void updateMyProfile_ShouldReturnBadRequestOnRuntimeException() {
        User details = new User();
        when(authentication.getName()).thenReturn("u1");
        when(userService.updateUser("u1", details)).thenThrow(new RuntimeException("Invalid data"));

        ResponseEntity<?> response = userController.updateMyProfile(details, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody());
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        List<User> users = List.of(new User(), new User());
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUserById_ShouldReturnUserWhenFound() {
        User user = new User();
        when(userService.getUserById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserById("u1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void getUserById_ShouldReturnNotFoundWhenMissing() {
        when(userService.getUserById("u1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById("u1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserById_ShouldReturnBadRequestOnException() {
        when(userService.getUserById("u1")).thenThrow(new RuntimeException("Database down"));

        ResponseEntity<?> response = userController.getUserById("u1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error retrieving user: Database down", response.getBody());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        User details = new User();
        User updated = new User();
        when(userService.updateUser("u1", details)).thenReturn(updated);

        ResponseEntity<?> response = userController.updateUser("u1", details);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void updateUser_ShouldReturnBadRequestOnRuntimeException() {
        User details = new User();
        when(userService.updateUser("u1", details)).thenThrow(new RuntimeException("Email exists"));

        ResponseEntity<?> response = userController.updateUser("u1", details);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email exists", response.getBody());
    }

    @Test
    void deleteUser_ShouldReturnOkWhenDeleted() {
        ResponseEntity<?> response = userController.deleteUser("u1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUser("u1");
    }

    @Test
    void deleteUser_ShouldReturnBadRequestOnRuntimeException() {
        doThrow(new RuntimeException("Cannot delete")).when(userService).deleteUser("u1");

        ResponseEntity<?> response = userController.deleteUser("u1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot delete", response.getBody());
    }
}
