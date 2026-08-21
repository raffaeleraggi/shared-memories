package com.sharedmemories.storage;

import com.sharedmemories.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

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

    private S3Client createClient() {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        return S3Client.builder()
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        r2.getAccessKey(),
                                        r2.getSecretKey()
                                )
                        )
                )
                .region(Region.of("auto"))
                .build();
    }

    private S3Presigner createPresigner() {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        return S3Presigner.builder()
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        r2.getAccessKey(),
                                        r2.getSecretKey()
                                )
                        )
                )
                .region(Region.of("auto"))
                .build();
    }

    @Override
    public MultipartUploadStart startMultipart(
            String storageKey,
            String contentType
    ) {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        try (S3Client client = createClient()) {

            CreateMultipartUploadResponse response =
                    client.createMultipartUpload(
                            CreateMultipartUploadRequest.builder()
                                    .bucket(r2.getBucket())
                                    .key(storageKey)
                                    .contentType(contentType)
                                    .build()
                    );

            return new MultipartUploadStart(
                    response.uploadId()
            );
        }
    }

    @Override
    public SignedPartUpload createPartUploadUrl(
            String storageKey,
            String uploadId,
            int partNumber
    ) {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        UploadPartRequest uploadPartRequest =
                UploadPartRequest.builder()
                        .bucket(r2.getBucket())
                        .key(storageKey)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

        UploadPartPresignRequest presignRequest =
                UploadPartPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .uploadPartRequest(uploadPartRequest)
                        .build();

        try (S3Presigner presigner = createPresigner()) {

            PresignedUploadPartRequest signed =
                    presigner.presignUploadPart(
                            presignRequest
                    );

            return new SignedPartUpload(
                    signed.url().toString(),
                    partNumber
            );
        }
    }

    @Override
    public void completeMultipart(
            String storageKey,
            String uploadId,
            List<CompletedUploadPart> parts
    ) {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        List<CompletedPart> completedParts =
                parts.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        CompletedUploadPart::partNumber
                                )
                        )
                        .map(part ->
                                CompletedPart.builder()
                                        .partNumber(part.partNumber())
                                        .eTag(part.eTag())
                                        .build()
                        )
                        .toList();

        try (S3Client client = createClient()) {

            client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(r2.getBucket())
                            .key(storageKey)
                            .uploadId(uploadId)
                            .multipartUpload(
                                    CompletedMultipartUpload.builder()
                                            .parts(completedParts)
                                            .build()
                            )
                            .build()
            );
        }
    }

    @Override
    public void abortMultipart(
            String storageKey,
            String uploadId
    ) {
        AppProperties.R2 r2 = properties.getStorage().getR2();

        try (S3Client client = createClient()) {

            client.abortMultipartUpload(
                    AbortMultipartUploadRequest.builder()
                            .bucket(r2.getBucket())
                            .key(storageKey)
                            .uploadId(uploadId)
                            .build()
            );
        }
    }
    @Override
    public void downloadTo(
            String storageKey,
            OutputStream outputStream
    ) {
        AppProperties.R2 r2 =
                properties.getStorage().getR2();

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(r2.getBucket())
                        .key(storageKey)
                        .build();

        try (S3Client client = createClient()) {

            client.getObject(
                    request,
                    ResponseTransformer.toOutputStream(
                            outputStream
                    )
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Errore download R2: " + storageKey,
                    e
            );
        }
    }

}
