package com.sharedmemories.uploadsource;

import com.sharedmemories.config.AppProperties;
import com.sharedmemories.event.EventEntity;
import com.sharedmemories.event.EventService;
import com.sharedmemories.qr.QrCodeService;
import com.sharedmemories.upload.UploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events/{eventId}/upload-sources")
@RequiredArgsConstructor
public class AdminUploadSourceController {

    private final UploadSourceService uploadSourceService;
    private final AppProperties properties;
    private final QrCodeService qrCodeService;
    private final EventService eventService;
    private final UploadSourceRepository uploadSourceRepository;

    @PostMapping("/generate-tables")
    public List<UploadSourceResponse> generateTables(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "13") int count
    ) {
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("Numero tavoli non valido");
        }

        return uploadSourceService.generateTables(eventId, count)
                .stream()
                .map(source -> toResponse(eventId, source))
                .toList();
    }

    @GetMapping
    public List<UploadSourceResponse> findAll(
            @PathVariable UUID eventId
    ) {
        return uploadSourceService.findByEvent(eventId)
                .stream()
                .map(source -> toResponse(eventId, source))
                .toList();
    }

    private UploadSourceResponse toResponse(
            UUID eventId,
            UploadSourceEntity source
    ) {
        String guestUrl = buildGuestUrl(source);

        String qrUrl =
                "/api/admin/events/"
                        + eventId
                        + "/upload-sources/"
                        + source.getId()
                        + "/qr";

        return new UploadSourceResponse(
                source.getId(),
                source.getLabel(),
                source.getToken(),
                guestUrl,
                qrUrl
        );
    }

    private String buildGuestUrl(UploadSourceEntity source) {
        return properties.getPublicBaseUrl()
                + "/e/"
                + source.getEvent().getSlug()
                + "/tavolo/"
                + source.getToken();
    }

    @GetMapping(
            value = "/{sourceId}/qr",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> qr(
            @PathVariable UUID eventId,
            @PathVariable UUID sourceId
    ) throws Exception {

        UploadSourceEntity source = getByIdAndEventId(sourceId, eventId);

        String url = properties.getPublicBaseUrl()
                + "/e/"
                + source.getEvent().getSlug()
                + "/tavolo/"
                + source.getToken();

        byte[] image = qrCodeService.generate(url);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(image.length)
                .body(image);
    }

    @Transactional
    public UploadSourceEntity getByIdAndEventId(
            UUID sourceId,
            UUID eventId
    ) {
        return uploadSourceRepository.findByIdAndEvent_Id(sourceId, eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "QR code non trovato per questo evento"
                        )
                );
    }
}