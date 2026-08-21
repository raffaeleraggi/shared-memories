package com.sharedmemories.storage;

public record MultipartAbortRequest(
        String storageKey,
        String uploadId
) {}
