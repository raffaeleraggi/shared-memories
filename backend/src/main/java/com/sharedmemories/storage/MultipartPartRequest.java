package com.sharedmemories.storage;

public record MultipartPartRequest(
        String storageKey,
        String uploadId,
        int partNumber
) {}
