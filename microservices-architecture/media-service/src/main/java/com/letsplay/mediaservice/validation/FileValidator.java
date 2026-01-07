package com.letsplay.mediaservice.validation;

import com.letsplay.mediaservice.exception.BadRequestException;
import com.letsplay.mediaservice.exception.InvalidFileTypeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class FileValidator {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        validateFileSize(file);
        validateFileType(file);
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    String.format("File size (%d bytes) exceeds maximum limit of 2MB (%d bytes)",
                            file.getSize(), MAX_FILE_SIZE)
            );
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException(
                    String.format("Invalid file type: %s. Allowed types: JPEG, PNG, GIF, WebP",
                            contentType != null ? contentType : "unknown")
            );
        }

        // Additional validation: check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.toLowerCase();
            if (!extension.endsWith(".jpg") && !extension.endsWith(".jpeg") &&
                !extension.endsWith(".png") && !extension.endsWith(".gif") &&
                !extension.endsWith(".webp")) {
                throw new InvalidFileTypeException(
                        "Invalid file extension. Allowed extensions: .jpg, .jpeg, .png, .gif, .webp"
                );
            }
        }
    }

    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public List<String> getAllowedContentTypes() {
        return ALLOWED_CONTENT_TYPES;
    }
}
