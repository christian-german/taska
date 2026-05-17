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

    @PostMapping
    public void registerDevice(@Valid @RequestBody RegisterDeviceRequest req) {
        log.debug("Registering device token: {}", req.token());
        DeviceToken deviceToken = deviceTokenRepository.findByToken(req.token())
                .orElseGet(DeviceToken::new);
        deviceToken.setToken(req.token());
        deviceTokenRepository.save(deviceToken);
        log.debug("Device token registered successfully");
    }
}
