package com.sharedmemories.upload;

public record CreateUploadUrlResponse(String uploadUrl, String storageKey, String publicUrl, String method) {}
