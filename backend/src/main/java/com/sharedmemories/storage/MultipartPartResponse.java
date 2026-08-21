package com.sharedmemories.storage;

public record MultipartPartResponse(
        String uploadUrl,
        int partNumber
) {}
