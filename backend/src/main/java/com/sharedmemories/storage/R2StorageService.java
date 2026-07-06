package com.sharedmemories.storage;

import com.sharedmemories.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "r2")
public class R2StorageService implements StorageService {
    private final AppProperties properties;

    @Override
    public SignedUpload createUploadUrl(String storageKey, String contentType) {
        AppProperties.R2 r2 = properties.getStorage().getR2();
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(r2.getAccessKey(), r2.getSecretKey())))
                .region(Region.of("auto"))
                .build()) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(r2.getBucket())
                    .key(storageKey)
                    .contentType(contentType)
                    .build();
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();
            return new SignedUpload(presigner.presignPutObject(presignRequest).url().toString(), "PUT");
        }
    }

    @Override
    public String publicUrl(String storageKey) {
        return properties.getStorage().getR2().getPublicBaseUrl() + "/" + storageKey;
    }
}
