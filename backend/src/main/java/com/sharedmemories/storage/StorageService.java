package com.sharedmemories.storage;

public interface StorageService {
    SignedUpload createUploadUrl(String storageKey, String contentType);
    String publicUrl(String storageKey);
}
