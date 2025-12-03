package com.samson.restbackend.models;

import com.samson.restbackend.util.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyKey {
    @Id
    @Column(name = "idem_key", nullable = false, updatable = false)
    private String key;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status; // IN_PROGRESS | SUCCEEDED | FAILED

    @Column(name = "resource_id")
    private UUID resourceId; // created user id

    @Column(name = "response_code")
    private Integer responseCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}
