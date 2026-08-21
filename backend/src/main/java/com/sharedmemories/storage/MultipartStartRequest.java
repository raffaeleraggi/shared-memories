package com.sharedmemories.storage;

public record MultipartStartRequest(
        String filename,
        String contentType,
        long size
) {}