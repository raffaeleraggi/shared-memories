package com.sharedmemories.event;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
