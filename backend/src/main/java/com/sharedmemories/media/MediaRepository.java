package com.sharedmemories.media;

import com.sharedmemories.event.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<MediaEntity, UUID> {
    List<MediaEntity> findByEventOrderByUploadedAtDesc(EventEntity event);
    List<MediaEntity> findByEventAndApprovedTrueOrderByUploadedAtDesc(EventEntity event);
}
