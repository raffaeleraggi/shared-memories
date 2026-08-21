package com.sharedmemories.storage;

public record SignedPartUpload(
        String uploadUrl,
        int partNumber
) {}
