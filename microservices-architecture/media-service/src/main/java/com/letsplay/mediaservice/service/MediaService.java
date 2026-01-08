package com.letsplay.mediaservice.service;

import com.letsplay.mediaservice.dto.MediaResponse;
import com.letsplay.mediaservice.exception.BadRequestException;
import com.letsplay.mediaservice.exception.ResourceNotFoundException;
import com.letsplay.mediaservice.model.Media;
import com.letsplay.mediaservice.repository.MediaRepository;
import com.letsplay.mediaservice.validation.FileValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;

    @Value("${app.base-url:https://localhost:8083}")
    private String baseUrl;

    public MediaService(MediaRepository mediaRepository, 
                       FileStorageService fileStorageService,
                       FileValidator fileValidator) {
        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
        this.fileValidator = fileValidator;
    }

    public MediaResponse uploadMedia(MultipartFile file, String productId, String userId, String userRole) {
        // Only sellers can upload media
        if (!"seller".equals(userRole)) {
            throw new BadRequestException("Only sellers can upload media");
        }

        // Validate file
        fileValidator.validate(file);

        // Store file
        String filePath = fileStorageService.storeFile(file);

        // Create media record
        Media media = new Media();
        media.setImagePath(filePath);
        media.setProductId(productId);
        media.setContentType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setOriginalFilename(file.getOriginalFilename());
        media.setUserId(userId);

        Media savedMedia = mediaRepository.save(media);

        return toMediaResponse(savedMedia);
    }

    public MediaResponse getMediaById(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        return toMediaResponse(media);
    }

    public Media getMediaEntityById(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
    }

    public List<MediaResponse> getMediaByProductId(String productId) {
        return mediaRepository.findByProductId(productId).stream()
                .map(this::toMediaResponse)
                .collect(Collectors.toList());
    }

    public void deleteMedia(String id, String userId, String userRole) {
        Media media = getMediaEntityById(id);

        // Check ownership
        if (!media.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own media");
        }

        // Delete file from storage
        fileStorageService.deleteFile(media.getImagePath());

        // Delete from database
        mediaRepository.delete(media);
    }

    public void deleteMediaByProductId(String productId) {
        List<Media> mediaList = mediaRepository.findByProductId(productId);
        
        for (Media media : mediaList) {
            fileStorageService.deleteFile(media.getImagePath());
        }
        
        mediaRepository.deleteByProductId(productId);
    }

    private MediaResponse toMediaResponse(Media media) {
        MediaResponse response = new MediaResponse();
        response.setId(media.getId());
        response.setProductId(media.getProductId());
        response.setContentType(media.getContentType());
        response.setFileSize(media.getFileSize());
        response.setOriginalFilename(media.getOriginalFilename());
        response.setUploadedAt(media.getUploadedAt());
        response.setUrl(baseUrl + "/api/media/" + media.getId() + "/download");
        return response;
    }
}
