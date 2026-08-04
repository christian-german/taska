package com.taska.domain.notification;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RegisterDeviceControllerTest {
    private final DeviceTokenRepository repository = mock(DeviceTokenRepository.class);
    private final RegisterDeviceController controller = new RegisterDeviceController(repository);

    @Test void storesTheAuthenticatedSubjectOnNewToken() {
        Jwt jwt = jwt("account-a");
        when(repository.findByToken("token")).thenReturn(Optional.empty());

        controller.registerDevice(new RegisterDeviceRequest("token"), jwt);

        var saved = ArgumentCaptor.forClass(DeviceToken.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getToken()).isEqualTo("token");
        assertThat(saved.getValue().getAccountSubject()).isEqualTo("account-a");
    }

    @Test void reassignsExistingTokenToCurrentAccount() {
        DeviceToken device = new DeviceToken();
        device.setToken("token");
        device.setAccountSubject("account-a");
        when(repository.findByToken("token")).thenReturn(Optional.of(device));

        controller.registerDevice(new RegisterDeviceRequest("token"), jwt("account-b"));

        verify(repository).save(device);
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
