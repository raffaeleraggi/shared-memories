package com.sharedmemories.upload;

import com.sharedmemories.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalUploadController {
    private final AppProperties properties;

    @PostMapping(value = "/api/public/uploads/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("key") String key, @RequestPart("file") MultipartFile file) throws Exception {
        if (!StringUtils.hasText(key) || key.contains("..")) throw new IllegalArgumentException("Storage key non valida");
        Path base = Path.of(properties.getStorage().getLocalPath()).toAbsolutePath().normalize();
        Path target = base.resolve(key).normalize();
        if (!target.startsWith(base)) throw new IllegalArgumentException("Storage key non valida");
        Files.createDirectories(target.getParent());
        file.transferTo(target);
    }
}
