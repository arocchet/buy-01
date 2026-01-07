package com.letsplay.mediaservice.controller;

import com.letsplay.mediaservice.dto.MediaResponse;
import com.letsplay.mediaservice.model.Media;
import com.letsplay.mediaservice.service.FileStorageService;
import com.letsplay.mediaservice.service.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;
    private final FileStorageService fileStorageService;

    public MediaController(MediaService mediaService, FileStorageService fileStorageService) {
        this.mediaService = mediaService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        String role = getRoleFromAuth(authentication);
        MediaResponse response = mediaService.uploadMedia(file, productId, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMedia(@PathVariable String id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMedia(@PathVariable String id) {
        Media media = mediaService.getMediaEntityById(id);
        Resource resource = fileStorageService.loadFileAsResource(media.getImagePath());
        String contentType = fileStorageService.getContentType(media.getImagePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + media.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<MediaResponse>> getMediaByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(mediaService.getMediaByProductId(productId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable String id,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        String role = getRoleFromAuth(authentication);
        mediaService.deleteMedia(id, userId, role);
        return ResponseEntity.noContent().build();
    }

    private String getUserIdFromAuth(Authentication authentication) {
        return authentication.getName();
    }

    private String getRoleFromAuth(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.replace("ROLE_", "").toLowerCase())
                .findFirst()
                .orElse("client");
    }
}
