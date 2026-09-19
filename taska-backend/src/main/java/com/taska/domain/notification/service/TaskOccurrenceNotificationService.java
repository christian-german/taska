package com.taska.domain.notification.service;

import com.taska.domain.notification.repository.TaskOccurrenceNotificationRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Claims and resets one-time notification delivery for recurring occurrences. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskOccurrenceNotificationService {

  private final TaskOccurrenceNotificationRepository taskOccurrenceNotificationRepository;

  /**
   * Attempts to atomically claim one recurring occurrence for notification delivery.
   *
   * @param seriesId recurring series identifier
   * @param occurrenceScheduledAt stable RRULE-generated occurrence identity
   * @return {@code true} only for the execution that inserted the unique marker
   */
  @Transactional
  public boolean claim(UUID seriesId, Instant occurrenceScheduledAt) {
    return taskOccurrenceNotificationRepository.claim(
            UUID.randomUUID(), seriesId, occurrenceScheduledAt, Instant.now())
        == 1;
  }

  /**
   * Restores notification eligibility after an occurrence's effective schedule changes.
   *
   * @param seriesId recurring series identifier
   * @param occurrenceScheduledAt stable RRULE-generated occurrence identity
   */
  @Transactional
  public void clear(UUID seriesId, Instant occurrenceScheduledAt) {
    taskOccurrenceNotificationRepository.deleteBySeriesIdAndOccurrenceScheduledAt(
        seriesId, occurrenceScheduledAt);
  }
}
