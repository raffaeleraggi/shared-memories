package com.sharedmemories.media;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record MediaDto(UUID id, String originalFilename, String publicUrl, String contentType, Long size, boolean approved, Instant uploadedAt, String uploadedBy, String message) {
    public static MediaDto from(MediaEntity m) {
        return new MediaDto(m.getId(), m.getOriginalFilename(), m.getPublicUrl(), m.getContentType(), m.getSize(), m.isApproved(), m.getUploadedAt(), m.getUploadedBy(), m.getMessage());
    }
}
