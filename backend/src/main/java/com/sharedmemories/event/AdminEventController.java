package com.sharedmemories.event;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sharedmemories.config.AppProperties;
import com.sharedmemories.media.MediaDto;
import com.sharedmemories.qr.QrCodeService;
import com.sharedmemories.upload.DownloadService;
import com.sharedmemories.upload.UploadService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class AdminEventController {
    private final EventService eventService;
    private final UploadService uploadService;
    private final AppProperties properties;
    private final DownloadService downloadService;
    private final QrCodeService qrCodeService;

    @PostMapping
    public EventDto create(@Valid @RequestBody CreateEventRequest request) { return eventService.create(request); }

    @DeleteMapping("/{id}")
    public void deleteEvento(@PathVariable UUID id){
        eventService.deleteById(id);
    }

    @GetMapping
    public List<EventDto> findAll() { return eventService.findAll(); }

    @GetMapping("/{id}/media")
    public List<MediaDto> media(@PathVariable UUID id) { return uploadService.adminGallery(id); }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable UUID id) throws Exception {
        EventEntity event = eventService.getById(id);
        String url = properties.getPublicBaseUrl() + "/e/" + event.getSlug();
        var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 360, 360);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

/*    @GetMapping("/media/download-selected")
    public void downloadSelected(@RequestParam List<UUID> ids, HttpServletResponse response) throws IOException {
        downloadService.downloadSelected(ids, response, properties);
    }

    @GetMapping("/media/download-all")
    public void downloadAll(HttpServletResponse response) throws IOException {
        downloadService.downloadAll(response, properties);
    }*/

    @GetMapping(
            value = "/media/download-selected",
            produces = "application/zip"
    )
    public void downloadSelected(
            @RequestParam List<UUID> ids,
            HttpServletResponse response
    ) throws IOException {

        downloadService.downloadSelected(ids, response);
    }

    @GetMapping(
            value = "/media/download-all",
            produces = "application/zip"
    )
    public void downloadAll(
            HttpServletResponse response
    ) throws IOException {

        downloadService.downloadAll(response);
    }



}
