package com.sharedmemories.uploadsource;

import com.sharedmemories.uploadsource.PublicUploadSourceResponse;
import com.sharedmemories.uploadsource.UploadSourceEntity;
import com.sharedmemories.uploadsource.UploadSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/upload-sources")
@RequiredArgsConstructor
public class PublicUploadSourceController {

    private final UploadSourceService uploadSourceService;

    @GetMapping("/{token}")
    public PublicUploadSourceResponse getByToken(
            @PathVariable String token
    ) {
        UploadSourceEntity source = uploadSourceService.getByToken(token);

        return new PublicUploadSourceResponse(
                source.getLabel(),
                source.getEvent().getName(),
                source.getEvent().getSlug()
        );
    }
}