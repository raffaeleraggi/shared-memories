package com.sharedmemories.upload;

import com.sharedmemories.config.AppProperties;
import com.sharedmemories.event.EventEntity;
import com.sharedmemories.event.EventService;
import com.sharedmemories.media.MediaDto;
import com.sharedmemories.media.MediaEntity;
import com.sharedmemories.media.MediaRepository;
import com.sharedmemories.storage.SignedUpload;
import com.sharedmemories.storage.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private static final long MAX_FILE_SIZE = 200L * 1024L * 1024L;
    private final EventService eventService;
    private final MediaRepository mediaRepository;
    private final StorageService storageService;

    public void downloadSelected(List<UUID> ids, HttpServletResponse response, AppProperties properties) throws IOException {
        List<MediaEntity> mediaList = mediaRepository.findAllById(ids);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"selected-media.zip\"");

        Path base = Path.of(properties.getStorage().getLocalPath()).toAbsolutePath().normalize();

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (MediaEntity media : mediaList) {
                Path filePath = base.resolve(media.getStorageKey()).normalize();

                if (!filePath.startsWith(base) || !Files.exists(filePath)) {
                    continue;
                }

                zipOut.putNextEntry(new ZipEntry(media.getOriginalFilename()));
                Files.copy(filePath, zipOut);
                zipOut.closeEntry();
            }

            zipOut.finish();
        }
    }

    public void downloadAll(HttpServletResponse response, AppProperties properties) throws IOException {
        List<MediaEntity> mediaList = mediaRepository.findAll();

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"all-media.zip\"");

        Path base = Path.of(properties.getStorage().getLocalPath()).toAbsolutePath().normalize();

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (MediaEntity media : mediaList) {
                Path filePath = base.resolve(media.getStorageKey()).normalize();

                if (!filePath.startsWith(base) || !Files.exists(filePath)) {
                    continue;
                }

                zipOut.putNextEntry(new ZipEntry(media.getOriginalFilename()));
                Files.copy(filePath, zipOut);
                zipOut.closeEntry();
            }

            zipOut.finish();
        }
    }
}
