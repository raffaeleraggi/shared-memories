package com.sharedmemories.uploadsource;

import java.util.UUID;

public record UploadSourceResponse(
        UUID id,
        String label,
        String token,
        String guestUrl,
        String qrUrl
) {
}