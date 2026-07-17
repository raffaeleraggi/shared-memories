package com.sharedmemories.uploadsource;

import com.sharedmemories.event.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadSourceRepository
        extends JpaRepository<UploadSourceEntity, UUID> {

    Optional<UploadSourceEntity> findByTokenAndActiveTrue(String token);

    List<UploadSourceEntity> findByEventOrderByLabelAsc(EventEntity event);

    boolean existsByEventAndLabel(EventEntity event, String label);

    Optional<UploadSourceEntity> findByIdAndEvent_Id(UUID sourceId, UUID eventId);
}