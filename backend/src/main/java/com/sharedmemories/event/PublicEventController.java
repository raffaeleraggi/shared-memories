package com.sharedmemories.event;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{slug}")
    public EventDto findBySlug(@PathVariable String slug) {
        return EventDto.from(eventService.getBySlug(slug));
    }
}
