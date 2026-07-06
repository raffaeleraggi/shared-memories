package com.sharedmemories.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUploadUrlRequest(@NotBlank String filename, @NotBlank String contentType, @NotNull Long size) {}
