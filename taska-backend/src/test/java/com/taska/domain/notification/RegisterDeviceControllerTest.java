package com.taska.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.taska.domain.notification.controller.RegisterDeviceController;
import com.taska.domain.notification.controller.RegisterDeviceRequest;
import com.taska.domain.notification.repository.DeviceToken;
import com.taska.domain.notification.repository.DeviceTokenRepository;
import com.taska.domain.notification.service.DeviceRegistrationService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;

class RegisterDeviceControllerTest {
  private final DeviceTokenRepository deviceTokenRepository = mock(DeviceTokenRepository.class);
  private final DeviceRegistrationService deviceRegistrationService =
      new DeviceRegistrationService(deviceTokenRepository);
  private final RegisterDeviceController registerDeviceController =
      new RegisterDeviceController(deviceRegistrationService);

  @Test
  void storesTheAuthenticatedSubjectOnNewToken() {
    Jwt jwt = jwt("account-a");
    when(deviceTokenRepository.findByToken("token")).thenReturn(Optional.empty());

    registerDeviceController.registerDevice(new RegisterDeviceRequest("token"), jwt);

    var saved = ArgumentCaptor.forClass(DeviceToken.class);
    verify(deviceTokenRepository).save(saved.capture());
    assertThat(saved.getValue().getToken()).isEqualTo("token");
    assertThat(saved.getValue().getAccountSubject()).isEqualTo("account-a");
  }

  @Test
  void reassignsExistingTokenToCurrentAccount() {
    DeviceToken device = new DeviceToken();
    device.setToken("token");
    device.setAccountSubject("account-a");
    when(deviceTokenRepository.findByToken("token")).thenReturn(Optional.of(device));

    registerDeviceController.registerDevice(new RegisterDeviceRequest("token"), jwt("account-b"));

    verify(deviceTokenRepository).save(device);
    assertThat(device.getAccountSubject()).isEqualTo("account-b");
  }

  private Jwt jwt(String subject) {
    return Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject(subject)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
  }
}
