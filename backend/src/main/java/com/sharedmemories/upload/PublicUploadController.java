package com.sharedmemories.upload;

import com.sharedmemories.media.MediaDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/events/{slug}")
public class PublicUploadController {
    private final UploadService uploadService;

    @PostMapping("/upload-url")
    public CreateUploadUrlResponse uploadUrl(@PathVariable String slug, @Valid @RequestBody CreateUploadUrlRequest request) {
        return uploadService.createUploadUrl(slug, request);
    }

    @PostMapping("/media/complete")
    public void completeUpload(
            @PathVariable String slug,
            @Valid @RequestBody CompleteUploadRequest request
    ) {
        uploadService.completeUpload(slug, request);
    }

    @GetMapping("/gallery")
    public List<MediaDto> gallery(@PathVariable String slug) {
        return uploadService.publicGallery(slug);
    }
}
