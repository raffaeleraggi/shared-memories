package com.sharedmemories.storage;

import java.util.List;

public interface StorageService {
    SignedUpload createUploadUrl(String storageKey, String contentType);
    String publicUrl(String storageKey);
    MultipartUploadStart startMultipart(String storageKey, String contentType);
    SignedPartUpload createPartUploadUrl(String storageKey, String uploadId, int partNumber);
    void completeMultipart(String storageKey, String uploadId, List<CompletedUploadPart> parts);
    void abortMultipart(String storageKey, String uploadId);
}
