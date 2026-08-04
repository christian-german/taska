package com.taska.domain.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a registered FCM device token in the {@code device_tokens} table.
 * <p>
 * Tokens are registered (or refreshed) when the mobile app calls the device-registration
 * endpoint. The registration performs an upsert: if a row with the same {@link #token} value
 * already exists (looked up via {@link DeviceTokenRepository#findByToken}), its timestamps are
 * updated; otherwise a new row is inserted. The token column is capped at 512 characters to
 * accommodate FCM token lengths.
 */
@Entity
@Table(name = "device_tokens")
@Getter
@Setter
public class DeviceToken {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The FCM registration token string; required, max 512 characters. */
    @Column(nullable = false, length = 512)
    private String token;

    /** OAuth2 JWT subject of the account that registered this device. */
    @Column(name = "account_subject", length = 255)
    private String accountSubject;

    /** Timestamp when the token was first registered; set by {@link #onCreate()} and never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the last refresh/update; updated automatically by {@link #onUpdate()}. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Initialises {@link #createdAt} and {@link #updatedAt} on first persist. */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /** Refreshes {@link #updatedAt} on every subsequent persist. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
