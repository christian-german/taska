package com.taska.domain.notification.service;

import com.taska.domain.notification.repository.DeviceToken;
import com.taska.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registers account-scoped notification devices. */
@Service
@RequiredArgsConstructor
public class DeviceRegistrationService {

  private static final Logger log = LoggerFactory.getLogger(DeviceRegistrationService.class);

  private final DeviceTokenRepository deviceTokenRepository;

  @Transactional
  public void register(String token, String accountSubject) {
    log.debug("Registering a device token");
    DeviceToken deviceToken = deviceTokenRepository.findByToken(token).orElseGet(DeviceToken::new);
    deviceToken.setToken(token);
    deviceToken.setAccountSubject(accountSubject);
    deviceTokenRepository.save(deviceToken);
    log.debug("Device token registered successfully");
  }
}
