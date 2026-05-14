package com.taska.domain.notification;

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

    private final DeviceTokenRepository deviceTokenRepository;

    @PostMapping
    public void registerDevice(@Valid @RequestBody RegisterDeviceRequest req) {
        DeviceToken deviceToken = deviceTokenRepository.findByToken(req.token())
                .orElseGet(DeviceToken::new);
        deviceToken.setToken(req.token());
        deviceTokenRepository.save(deviceToken);
    }
}
