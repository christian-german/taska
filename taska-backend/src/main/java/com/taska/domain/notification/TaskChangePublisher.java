package com.taska.domain.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/** Publishes opaque task-list invalidation events to one account's Android devices. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskChangePublisher {

    public static final String EVENT_TYPE = "tasks_changed";

    private final DeviceTokenRepository deviceTokenRepository;

    public void publishFor(String accountSubject) {
        if (accountSubject == null || accountSubject.isBlank()) return;

        for (DeviceToken device : deviceTokenRepository.findByAccountSubject(accountSubject)) {
            Message message = Message.builder()
                    .setToken(device.getToken())
                    .putData("event", EVENT_TYPE)
                    .build();
            try {
                var send = FirebaseMessaging.getInstance().sendAsync(message);
                send.addListener(() -> removeInvalidToken(device, send), Runnable::run);
            } catch (IllegalStateException exception) {
                log.warn("Firebase is unavailable; skipping task-change event", exception);
                return;
            }
        }
    }

    private void removeInvalidToken(DeviceToken device, java.util.concurrent.Future<?> send) {
        try {
            send.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof FirebaseMessagingException firebaseException
                    && firebaseException.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                deviceTokenRepository.delete(device);
            } else {
                log.warn("Could not send task-change event to device {}", device.getId(), cause);
            }
        }
    }
}
