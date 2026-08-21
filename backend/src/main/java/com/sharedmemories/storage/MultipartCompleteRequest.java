package com.sharedmemories.storage;

import java.util.List;

public record MultipartCompleteRequest(
        String storageKey,
        String uploadId,
        List<CompletedUploadPart> parts,
        String filename,
        String contentType,
        long size,
        String sourceToken
) {}