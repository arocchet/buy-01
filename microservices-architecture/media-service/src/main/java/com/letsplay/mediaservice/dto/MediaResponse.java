package com.letsplay.mediaservice.dto;

import java.time.LocalDateTime;

public class MediaResponse {
    private String id;
    private String productId;
    private String contentType;
    private Long fileSize;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private String url;

    public MediaResponse() {}

    public MediaResponse(String id, String productId, String contentType, Long fileSize, 
                        String originalFilename, LocalDateTime uploadedAt, String url) {
        this.id = id;
        this.productId = productId;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.originalFilename = originalFilename;
        this.uploadedAt = uploadedAt;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
