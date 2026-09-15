package com.taska.domain.notification.repository;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a registered FCM device token in the {@code device_tokens} table.
 *
 * <p>Tokens are registered (or refreshed) when the mobile app calls the device-registration
 * endpoint. The registration performs an upsert: if a row with the same {@link #token} value
 * already exists (looked up via {@link DeviceTokenRepository#findByToken}), its timestamps are
 * updated; otherwise a new row is inserted. The token column is capped at 512 characters to
 * accommodate FCM token lengths.
 */
@Entity
@Table(name = "device_tokens")
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

  /**
   * Timestamp when the token was first registered; set by {@link #onCreate()} and never updated.
   */
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

  public UUID getId() {
    return this.id;
  }

  public String getToken() {
    return this.token;
  }

  public String getAccountSubject() {
    return this.accountSubject;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public void setToken(final String token) {
    this.token = token;
  }

  public void setAccountSubject(final String accountSubject) {
    this.accountSubject = accountSubject;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
