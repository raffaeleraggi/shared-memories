package com.sharedmemories.event;

import java.time.LocalDate;
import java.util.UUID;

public record EventDto(UUID id, String name, String slug, String description, LocalDate eventDate, String coverImageUrl, boolean active) {
    public static EventDto from(EventEntity e) {
        return new EventDto(e.getId(), e.getName(), e.getSlug(), e.getDescription(), e.getEventDate(), e.getCoverImageUrl(), e.isActive());
    }
}
