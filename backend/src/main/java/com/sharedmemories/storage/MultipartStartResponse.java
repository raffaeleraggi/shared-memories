package com.sharedmemories.storage;

public record MultipartStartResponse(
        String storageKey,
        String uploadId
) {}
