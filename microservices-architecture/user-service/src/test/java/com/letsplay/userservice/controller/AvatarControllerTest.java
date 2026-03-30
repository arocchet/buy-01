package com.letsplay.userservice.controller;

import com.letsplay.userservice.exception.BadRequestException;
import com.letsplay.userservice.exception.ResourceNotFoundException;
import com.letsplay.userservice.model.User;
import com.letsplay.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvatarControllerTest {

    @TempDir
    Path tempDir;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private AvatarController avatarController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        avatarController = new AvatarController(userRepository);
        ReflectionTestUtils.setField(avatarController, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadAvatar_ShouldThrowWhenUserNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> avatarController.uploadAvatar("u1", file, authentication));
    }

    @Test
    void uploadAvatar_ShouldThrowWhenNotOwner() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("other@example.com");

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        assertThrows(BadRequestException.class, () -> avatarController.uploadAvatar("u1", file, authentication));
    }

    @Test
    void uploadAvatar_ShouldThrowWhenRoleIsNotSeller() {
        User user = new User("Alice", "alice@example.com", "pwd", "client");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        assertThrows(BadRequestException.class, () -> avatarController.uploadAvatar("u1", file, authentication));
    }

    @Test
    void uploadAvatar_ShouldThrowWhenFileEmpty() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");

        MockMultipartFile emptyFile = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);
        assertThrows(BadRequestException.class, () -> avatarController.uploadAvatar("u1", emptyFile, authentication));
    }

    @Test
    void uploadAvatar_ShouldThrowWhenFileTypeInvalid() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");

        MockMultipartFile invalidType = new MockMultipartFile("file", "avatar.txt", "text/plain", "data".getBytes());
        assertThrows(BadRequestException.class, () -> avatarController.uploadAvatar("u1", invalidType, authentication));
    }

    @Test
    void uploadAvatar_ShouldUploadAndReturnSuccess() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.save(user)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

        ResponseEntity<?> response = avatarController.uploadAvatar("u1", file, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Avatar uploaded successfully"));
        assertNotNull(user.getAvatar());
        assertTrue(Files.exists(Path.of(user.getAvatar())));
    }

    @Test
    void getAvatar_ShouldThrowWhenAvatarMissing() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        user.setAvatar(null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> avatarController.getAvatar("u1"));
    }

    @Test
    void getAvatar_ShouldReturnResourceWhenFileExists() throws IOException {
        Path avatarFile = tempDir.resolve("avatar.png");
        Files.writeString(avatarFile, "image-bytes");

        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        user.setAvatar(avatarFile.toString());
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        ResponseEntity<Resource> response = avatarController.getAvatar("u1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteAvatar_ShouldDeleteAndReturnSuccess() throws IOException {
        Path avatarFile = tempDir.resolve("avatar-delete.png");
        Files.writeString(avatarFile, "image-bytes");

        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        user.setAvatar(avatarFile.toString());

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.save(user)).thenReturn(user);

        ResponseEntity<?> response = avatarController.deleteAvatar("u1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Avatar deleted successfully"));
        assertNull(user.getAvatar());
        verify(userRepository).save(user);
    }

    @Test
    void deleteAvatar_ShouldThrowWhenNotOwner() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        user.setAvatar(tempDir.resolve("x.png").toString());

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("other@example.com");

        assertThrows(BadRequestException.class, () -> avatarController.deleteAvatar("u1", authentication));
    }

    @Test
    void uploadAvatar_ShouldThrowWhenFileTooLarge() {
        User user = new User("Alice", "alice@example.com", "pwd", "seller");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("alice@example.com");

        MultipartFile largeFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(2L * 1024 * 1024 + 1);
        when(largeFile.getContentType()).thenReturn("image/png");

        assertThrows(BadRequestException.class, () -> avatarController.uploadAvatar("u1", largeFile, authentication));
    }
}
