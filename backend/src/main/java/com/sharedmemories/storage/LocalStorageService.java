package com.sharedmemories.storage;

import com.sharedmemories.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
    private final AppProperties properties;

    @Override
    public SignedUpload createUploadUrl(String storageKey, String contentType) {
        return new SignedUpload("/api/public/uploads/local?key=" + storageKey, "POST");
    }

    @Override
    public String publicUrl(String storageKey) {
        return properties.getStorage().getLocalBaseUrl() + "/" + storageKey;
    }

}
