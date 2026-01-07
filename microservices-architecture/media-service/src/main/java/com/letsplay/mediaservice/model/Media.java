package com.letsplay.mediaservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "media")
public class Media {
    @Id
    private String id;

    @NotBlank(message = "Image path is mandatory")
    private String imagePath;

    @NotBlank(message = "Product ID is mandatory")
    private String productId;

    private String contentType;
    private Long fileSize;
    private String originalFilename;
    private LocalDateTime uploadedAt;

    // Owner user ID for authorization
    private String userId;

    public Media() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Media(String imagePath, String productId, String contentType, Long fileSize, String originalFilename, String userId) {
        this.imagePath = imagePath;
        this.productId = productId;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.originalFilename = originalFilename;
        this.userId = userId;
        this.uploadedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
