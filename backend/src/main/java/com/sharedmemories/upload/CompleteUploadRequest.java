package com.sharedmemories.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteUploadRequest(@NotBlank String storageKey, @NotBlank String filename, @NotBlank String contentType, @NotNull Long size) {}
