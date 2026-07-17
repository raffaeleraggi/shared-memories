package com.sharedmemories.uploadsource;

public record PublicUploadSourceResponse(
        String label,
        String eventName,
        String eventSlug
) {
}