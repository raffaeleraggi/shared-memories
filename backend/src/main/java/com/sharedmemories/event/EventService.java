package com.sharedmemories.event;

import com.sharedmemories.common.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    @Transactional
    public EventDto create(CreateEventRequest request) {
        EventEntity event = new EventEntity();
        event.setName(request.name());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        event.setCoverImageUrl(request.coverImageUrl());
        event.setSlug(uniqueSlug(request.name()));
        return EventDto.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<EventDto> findAll() {
        return eventRepository.findAll().stream().map(EventDto::from).toList();
    }

    @Transactional(readOnly = true)
    public EventEntity getBySlug(String slug) {
        return eventRepository.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("Evento non trovato"));
    }

    @Transactional(readOnly = true)
    public EventEntity getById(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Evento non trovato"));
    }

    @Transactional
    public void deleteById(UUID id) {
        eventRepository.deleteById(id);
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int counter = 2;
        while (eventRepository.existsBySlug(slug)) slug = base + "-" + counter++;
        return slug;
    }
}
