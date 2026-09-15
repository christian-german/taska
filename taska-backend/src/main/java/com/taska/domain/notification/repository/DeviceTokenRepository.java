package com.taska.domain.notification.repository;

import com.taska.domain.notification.DeviceToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for {@link DeviceToken} entities. */
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

  /**
   * Looks up a device token by its FCM token string. Used during device registration to determine
   * whether to insert a new record or update the timestamps of an existing one (upsert pattern).
   */
  Optional<DeviceToken> findByToken(String deviceToken);

  List<DeviceToken> findByAccountSubject(String accountSubject);
}
