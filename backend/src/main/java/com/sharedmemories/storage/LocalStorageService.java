package com.sharedmemories.storage;

import com.sharedmemories.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Override
    public MultipartUploadStart startMultipart(
            String storageKey,
            String contentType
    ) {
        throw new UnsupportedOperationException(
                "Multipart upload non supportato in modalità local"
        );
    }

    @Override
    public SignedPartUpload createPartUploadUrl(
            String storageKey,
            String uploadId,
            int partNumber)
    {
        throw new UnsupportedOperationException(
                "Multipart upload non supportato in modalità local"
        );
    }

    @Override
    public void completeMultipart(
            String storageKey,
            String uploadId,
            List<CompletedUploadPart> parts
    ){
        throw new UnsupportedOperationException(
                "Multipart upload non supportato in modalità local"
        );
    }

    @Override
    public void abortMultipart(String storageKey,
                               String uploadId
    ){
        throw new UnsupportedOperationException(
                "Multipart upload non supportato in modalità local"
        );
    }

    @Override
    public void downloadTo(
            String storageKey,
            OutputStream outputStream
    ) {
        try {
            Path file = Path.of(
                    properties.getStorage().getLocalPath()
            ).resolve(storageKey);

            Files.copy(file, outputStream);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Errore lettura file " + storageKey,
                    e
            );
        }
    }

}
