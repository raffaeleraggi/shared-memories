package com.sharedmemories.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteUploadRequest(

        @NotBlank
        String storageKey,

        @NotBlank
        String filename,

        @NotBlank
        String contentType,

        @NotNull
        Long size,

        @Size(max = 64)
        String sourceToken

) {
}