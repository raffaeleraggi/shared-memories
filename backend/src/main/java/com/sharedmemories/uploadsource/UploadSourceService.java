package com.sharedmemories.uploadsource;

import com.sharedmemories.event.EventEntity;
import com.sharedmemories.event.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadSourceService {

    private final UploadSourceRepository repository;
    private final EventService eventService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public List<UploadSourceEntity> generateTables(
            UUID eventId,
            int numberOfTables
    ) {
        EventEntity event = eventService.getById(eventId);

        for (int tableNumber = 1; tableNumber <= numberOfTables; tableNumber++) {
            String label = "Tavolo " + tableNumber;

            if (repository.existsByEventAndLabel(event, label)) {
                continue;
            }

            UploadSourceEntity source = new UploadSourceEntity();
            source.setEvent(event);
            source.setLabel(label);
            source.setToken(generateToken());

            repository.save(source);
        }

        return repository.findByEventOrderByLabelAsc(event);
    }

    @Transactional(readOnly = true)
    public UploadSourceEntity getByToken(String token) {
        return repository.findByTokenAndActiveTrue(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("QR code non valido o disabilitato")
                );
    }

    @Transactional(readOnly = true)
    public List<UploadSourceEntity> findByEvent(UUID eventId) {
        EventEntity event = eventService.getById(eventId);
        return repository.findByEventOrderByLabelAsc(event);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

}