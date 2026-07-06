package com.sharedmemories.event;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "events")
public class EventEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String slug;
    @Column(length = 1000)
    private String description;
    private LocalDate eventDate;
    private String coverImageUrl;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
