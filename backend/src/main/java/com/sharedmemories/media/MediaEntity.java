package com.sharedmemories.media;

import com.sharedmemories.event.EventEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "media")
public class MediaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EventEntity event;
    private String originalFilename;
    @Column(nullable = false, unique = true)
    private String storageKey;
    private String publicUrl;
    private String contentType;
    private Long size;
    private boolean approved = true;
    private Instant uploadedAt = Instant.now();
    @Column(length = 100)
    private String uploadedBy;
    @Column(length = 1000)
    private String message;
}
