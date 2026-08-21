package com.sharedmemories.storage;

public record CompletedUploadPart(
        int partNumber,
        String eTag
) {}
