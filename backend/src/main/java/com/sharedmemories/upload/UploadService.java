package com.sharedmemories.upload;

import com.sharedmemories.config.AppProperties;
import com.sharedmemories.event.EventEntity;
import com.sharedmemories.event.EventService;
import com.sharedmemories.media.MediaDto;
import com.sharedmemories.media.MediaEntity;
import com.sharedmemories.media.MediaRepository;
import com.sharedmemories.storage.*;
import com.sharedmemories.uploadsource.UploadSourceEntity;
import com.sharedmemories.uploadsource.UploadSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final long MAX_FILE_SIZE = 200L * 1024L * 1024L;
    private static final long MULTIPART_MAX_SIZE = 10L * 1024 * 1024 * 1024;
    private final EventService eventService;
    private final MediaRepository mediaRepository;
    private final StorageService storageService;
    private final AppProperties properties;
    private final UploadSourceService uploadSourceService;


    public CreateUploadUrlResponse createUploadUrl(String slug, CreateUploadUrlRequest request) {
        EventEntity event = eventService.getBySlug(slug);
        validateFile(request.contentType(), request.size());
        String extension = extensionOf(request.filename());
        String storageKey = "events/%s/%s%s".formatted(event.getSlug(), UUID.randomUUID(), extension);
        SignedUpload signedUpload = storageService.createUploadUrl(storageKey, request.contentType());
        return new CreateUploadUrlResponse(signedUpload.uploadUrl(), storageKey, storageService.publicUrl(storageKey), signedUpload.method());
    }

    @Transactional
    public MediaDto complete(String slug, CompleteUploadRequest request) {
        EventEntity event = eventService.getBySlug(slug);
        MediaEntity media = new MediaEntity();
        media.setEvent(event);
        media.setOriginalFilename(request.filename());
        media.setStorageKey(request.storageKey());
        media.setPublicUrl(storageService.publicUrl(request.storageKey()));
        media.setContentType(request.contentType());
        media.setSize(request.size());
        return MediaDto.from(mediaRepository.save(media));
    }

    @Transactional(readOnly = true)
    public List<MediaDto> publicGallery(String slug) {
        EventEntity event = eventService.getBySlug(slug);
        return mediaRepository.findByEventAndApprovedTrueOrderByUploadedAtDesc(event).stream().map(MediaDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MediaDto> adminGallery(UUID eventId) {
        EventEntity event = eventService.getById(eventId);
        return mediaRepository.findByEventOrderByUploadedAtDesc(event).stream().map(MediaDto::from).toList();
    }

    private void validateFile(String contentType, Long size) {
        if (size == null || size <= 0) throw new IllegalArgumentException("File non valido");
        if (!(contentType.startsWith("image/") || contentType.startsWith("video/"))) throw new IllegalArgumentException("Sono ammessi solo foto e video");
    }

    private String extensionOf(String filename) {
        int i = filename.lastIndexOf('.');
        return i > -1 ? filename.substring(i) : "";
    }


    @Transactional
    public void completeUpload(
            String slug,
            CompleteUploadRequest request
    ) {
        EventEntity event = eventService.getBySlug(slug);

        UploadSourceEntity source =
                uploadSourceService.getByToken(
                        request.sourceToken()
                );

        if (!source.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException(
                    "Il QR code non appartiene a questo evento"
            );
        }

        MediaEntity media = new MediaEntity();
        media.setEvent(event);
        media.setStorageKey(request.storageKey());
        media.setOriginalFilename(request.filename());
        media.setContentType(request.contentType());
        media.setSize(request.size());
        media.setPublicUrl(
                buildPublicUrl(request.storageKey())
        );

        media.setUploadedBy(source.getLabel());
        media.setMessage(null);
        media.setApproved(true);

        mediaRepository.save(media);
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String buildPublicUrl(String storageKey) {

        if ("local".equalsIgnoreCase(properties.getStorage().getMode())) {
            return properties.getStorage().getLocalBaseUrl()
                    + "/"
                    + storageKey;
        }

        return properties.getStorage()
                .getR2()
                .getPublicBaseUrl()
                + "/"
                + storageKey;
    }

    public MultipartStartResponse startMultipart(
            String slug,
            MultipartStartRequest request
    ) {
        EventEntity event = eventService.getBySlug(slug);

        String storageKey = buildStorageKey(
                event,
                request.filename()
        );

        MultipartUploadStart started =
                storageService.startMultipart(
                        storageKey,
                        request.contentType()
                );

        return new MultipartStartResponse(
                storageKey,
                started.uploadId()
        );
    }

    public MultipartPartResponse createPartUrl(
            String slug,
            MultipartPartRequest request
    ) {
        eventService.getBySlug(slug);

        SignedPartUpload signed =
                storageService.createPartUploadUrl(
                        request.storageKey(),
                        request.uploadId(),
                        request.partNumber()
                );

        return new MultipartPartResponse(
                signed.uploadUrl(),
                signed.partNumber()
        );
    }

    @Transactional
    public void completeMultipart(
            String slug,
            MultipartCompleteRequest request
    ) {
        EventEntity event =
                eventService.getBySlug(slug);

        UploadSourceEntity source =
                uploadSourceService.getByToken(
                        request.sourceToken()
                );

        if (!source.getEvent()
                .getId()
                .equals(event.getId())) {

            throw new IllegalArgumentException(
                    "QR code non appartenente all'evento"
            );
        }

        storageService.completeMultipart(
                request.storageKey(),
                request.uploadId(),
                request.parts()
        );

        MediaEntity media = new MediaEntity();

        media.setEvent(event);
        media.setStorageKey(request.storageKey());
        media.setOriginalFilename(request.filename());
        media.setContentType(request.contentType());
        media.setSize(request.size());

        media.setPublicUrl(
                storageService.publicUrl(
                        request.storageKey()
                )
        );

        media.setUploadedBy(source.getLabel());
        media.setApproved(true);

        mediaRepository.save(media);
    }

    public void abortMultipart(
            String slug,
            MultipartAbortRequest request
    ) {
        eventService.getBySlug(slug);

        storageService.abortMultipart(
                request.storageKey(),
                request.uploadId()
        );
    }

    private String buildStorageKey(EventEntity event, String filename) {
        String extension = extensionOf(filename);

        return "events/%s/%s%s".formatted(
                event.getSlug(),
                UUID.randomUUID(),
                extension
        );
    }
}
