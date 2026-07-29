package com.taska.domain.notification;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for registering a device to receive push notifications.
 * If the token is already registered it is upserted, so this endpoint is safe to call
 * on every app launch.
 *
 * @param token Firebase Cloud Messaging (FCM) device token; must not be blank
 */
public record RegisterDeviceRequest(
        @NotBlank String token
) {
}
