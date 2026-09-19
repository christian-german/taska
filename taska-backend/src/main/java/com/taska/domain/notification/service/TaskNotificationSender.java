package com.taska.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

/** Sends one task notification to one registered Firebase device token. */
@Service
public class TaskNotificationSender {

  /**
   * Submits one Firebase data message for a notification candidate.
   *
   * <p>Recurring payloads include their stable occurrence identity so clients can distinguish the
   * notified occurrence from its backing series.
   *
   * @param token destination Firebase device token
   * @param candidate effective task or occurrence values to send
   */
  public void send(String token, TaskNotificationCandidate candidate) {
    Message.Builder message =
        Message.builder()
            .setToken(token)
            .putData("task_id", candidate.taskId().toString())
            .putData("title", candidate.content() + " dans 15 min")
            .putData("body", candidate.description() != null ? candidate.description() : "");
    if (candidate.recurringOccurrence()) {
      message.putData("occurrence_scheduled_at", candidate.occurrenceScheduledAt().toString());
    }
    FirebaseMessaging.getInstance().sendAsync(message.build());
  }
}
