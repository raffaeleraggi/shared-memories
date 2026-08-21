package com.sharedmemories.upload;

import com.sharedmemories.media.MediaEntity;
import com.sharedmemories.media.MediaRepository;
import com.sharedmemories.storage.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final MediaRepository mediaRepository;
    private final StorageService storageService;

    public void downloadSelected(
            List<UUID> ids,
            HttpServletResponse response
    ) throws IOException {

        List<MediaEntity> mediaList =
                mediaRepository.findAllById(ids);

        writeZip(
                mediaList,
                response,
                "selected-media.zip"
        );
    }

    public void downloadAll(
            HttpServletResponse response
    ) throws IOException {

        List<MediaEntity> mediaList =
                mediaRepository.findAll();

        writeZip(
                mediaList,
                response,
                "all-media.zip"
        );
    }

    private void writeZip(
            List<MediaEntity> mediaList,
            HttpServletResponse response,
            String zipFilename
    ) throws IOException {

        response.setContentType("application/zip");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + zipFilename + "\""
        );

        Set<String> usedNames = new HashSet<>();

        try (
                ZipOutputStream zipOut =
                        new ZipOutputStream(
                                response.getOutputStream()
                        )
        ) {

            for (MediaEntity media : mediaList) {

                String entryName =
                        uniqueFilename(
                                media.getOriginalFilename(),
                                usedNames
                        );

                zipOut.putNextEntry(
                        new ZipEntry(entryName)
                );

                storageService.downloadTo(
                        media.getStorageKey(),
                        zipOut
                );

                zipOut.closeEntry();
            }

            zipOut.finish();
        }
    }

    private String uniqueFilename(
            String originalFilename,
            Set<String> usedNames
    ) {

        if (originalFilename == null ||
                originalFilename.isBlank()) {

            originalFilename = "file";
        }

        String candidate =
                originalFilename;

        int counter = 1;

        while (usedNames.contains(candidate)) {

            int dot =
                    originalFilename.lastIndexOf('.');

            if (dot > 0) {

                String name =
                        originalFilename.substring(
                                0,
                                dot
                        );

                String extension =
                        originalFilename.substring(dot);

                candidate =
                        name +
                                "_" +
                                counter +
                                extension;

            } else {

                candidate =
                        originalFilename +
                                "_" +
                                counter;
            }

            counter++;
        }

        usedNames.add(candidate);

        return candidate;
    }
}