package com.letsplay.userservice.controller;

import com.letsplay.userservice.exception.BadRequestException;
import com.letsplay.userservice.exception.ResourceNotFoundException;
import com.letsplay.userservice.model.User;
import com.letsplay.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class AvatarController {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    private final UserRepository userRepository;

    public AvatarController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user is the owner
        if (!authentication.getName().equals(user.getEmail())) {
            throw new BadRequestException("You can only upload your own avatar");
        }

        // Check if user is a seller
        if (!"seller".equals(user.getRole())) {
            throw new BadRequestException("Only sellers can upload avatars");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Delete old avatar if exists
            if (user.getAvatar() != null) {
                Path oldAvatarPath = Paths.get(user.getAvatar());
                Files.deleteIfExists(oldAvatarPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user avatar path
            user.setAvatar(filePath.toString());
            userRepository.save(user);

            return ResponseEntity.ok().body("{\"message\": \"Avatar uploaded successfully\", \"avatar\": \"" + newFilename + "\"}");

        } catch (IOException e) {
            throw new BadRequestException("Failed to upload avatar: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getAvatar() == null) {
            throw new ResourceNotFoundException("Avatar not found");
        }

        try {
            Path filePath = Paths.get(user.getAvatar());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Avatar file not found");
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Avatar not found");
        } catch (IOException e) {
            throw new BadRequestException("Failed to read avatar");
        }
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<?> deleteAvatar(@PathVariable String id, Authentication authentication) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user is the owner
        if (!authentication.getName().equals(user.getEmail())) {
            throw new BadRequestException("You can only delete your own avatar");
        }

        if (user.getAvatar() == null) {
            throw new ResourceNotFoundException("No avatar to delete");
        }

        try {
            Path filePath = Paths.get(user.getAvatar());
            Files.deleteIfExists(filePath);

            user.setAvatar(null);
            userRepository.save(user);

            return ResponseEntity.ok().body("{\"message\": \"Avatar deleted successfully\"}");
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete avatar: " + e.getMessage());
        }
    }
}
