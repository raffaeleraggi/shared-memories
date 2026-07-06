package com.sharedmemories.event;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateEventRequest(@NotBlank String name, String description, LocalDate eventDate, String coverImageUrl) {}
