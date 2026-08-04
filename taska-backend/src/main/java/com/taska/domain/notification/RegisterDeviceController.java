package com.taska.domain.notification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register-device")
@RequiredArgsConstructor
@Slf4j
public class RegisterDeviceController {

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Registers a device token for push notifications. If the token already exists it is
     * upserted rather than duplicated. Returns HTTP 200 on success.
     *
     * @param req the registration payload containing the FCM device token
     */
    @PostMapping
    public void registerDevice(@Valid @RequestBody RegisterDeviceRequest req,
                               @AuthenticationPrincipal Jwt jwt) {
        log.debug("Registering device token: {}", req.token());
        DeviceToken deviceToken = deviceTokenRepository.findByToken(req.token())
                .orElseGet(DeviceToken::new);
        deviceToken.setToken(req.token());
        deviceToken.setAccountSubject(jwt.getSubject());
        deviceTokenRepository.save(deviceToken);
        log.debug("Device token registered successfully");
    }
}
