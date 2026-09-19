package com.taska.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.repository.TaskOccurrenceNotificationRepository;
import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskOccurrenceNotificationServiceTest {

  @Mock private TaskOccurrenceNotificationRepository repository;
  @InjectMocks private TaskOccurrenceNotificationService service;

  @Test
  void claimReturnsTrueWhenTheRepositoryWinsTheAtomicInsert() {
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    when(repository.claim(any(), eq(seriesId), eq(occurrenceScheduledAt), any())).thenReturn(1);

    assertThat(service.claim(seriesId, occurrenceScheduledAt)).isTrue();
  }

  @Test
  void claimReturnsFalseWhenAnotherExecutionAlreadyClaimedTheOccurrence() {
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    when(repository.claim(any(), eq(seriesId), eq(occurrenceScheduledAt), any())).thenReturn(0);

    assertThat(service.claim(seriesId, occurrenceScheduledAt)).isFalse();
  }

  @Test
  void clearRemovesTheMarkerForTheStableOccurrenceIdentity() {
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    service.clear(seriesId, occurrenceScheduledAt);

    verify(repository).deleteBySeriesIdAndOccurrenceScheduledAt(seriesId, occurrenceScheduledAt);
  }
}
