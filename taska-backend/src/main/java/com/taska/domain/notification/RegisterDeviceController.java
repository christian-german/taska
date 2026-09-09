package com.taska.domain.notification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(RegisterDeviceController.class);

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Registers a device token for push notifications. If the token already exists it is
     * upserted rather than duplicated. Returns HTTP 200 on success.
     *
     * @param registerDeviceRequest the registration payload containing the FCM device token
     */
    @PostMapping
    public void registerDevice(@Valid @RequestBody RegisterDeviceRequest registerDeviceRequest,
                               @AuthenticationPrincipal Jwt jwt) {
        log.debug("Registering device token: {}", registerDeviceRequest.token());
        DeviceToken deviceToken = deviceTokenRepository.findByToken(registerDeviceRequest.token())
                .orElseGet(DeviceToken::new);
        deviceToken.setToken(registerDeviceRequest.token());
        deviceToken.setAccountSubject(jwt.getSubject());
        deviceTokenRepository.save(deviceToken);
        log.debug("Device token registered successfully");
    }
}
