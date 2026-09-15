package com.taska.domain.notification.controller;

import com.taska.domain.notification.service.DeviceRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register-device")
@RequiredArgsConstructor
public class RegisterDeviceController {

  private final DeviceRegistrationService deviceRegistrationService;

  /**
   * Registers a device token for push notifications. If the token already exists, it is upserted
   * rather than duplicated. Returns HTTP 200 on success.
   *
   * @param registerDeviceRequest the registration payload containing the FCM device token
   */
  @PostMapping
  public void registerDevice(
      @Valid @RequestBody RegisterDeviceRequest registerDeviceRequest,
      @AuthenticationPrincipal Jwt jwt) {
    deviceRegistrationService.register(registerDeviceRequest.token(), jwt.getSubject());
  }
}
