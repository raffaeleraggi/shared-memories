package com.sharedmemories.uploadsource;

import com.sharedmemories.event.EventEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "upload_sources", uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_upload_source_event_label",
                        columnNames = {"event_id", "label"}
                ),
                @UniqueConstraint(
                        name = "uk_upload_source_token",
                        columnNames = "token"
                )
        }
)
public class UploadSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
