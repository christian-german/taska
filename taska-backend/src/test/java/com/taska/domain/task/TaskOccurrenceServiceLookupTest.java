package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.config.TaskaProperties;
import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.repository.TaskRepository;
import com.taska.domain.task.definition.service.TaskDefinitionService;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceState;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskOccurrenceService;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.TaskResult;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskOccurrenceServiceLookupTest {

  private static final Instant OCCURRENCE = Instant.parse("2026-05-20T10:00:00Z");

  @Mock private TaskDefinitionService taskService;
  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private TaskRecurrenceService taskRecurrenceService;
  @Mock private TaskaProperties taskaProperties;
  @Mock private TaskOccurrenceNotificationService taskOccurrenceNotificationService;

  private TaskOccurrenceService taskOccurrenceService;

  @BeforeEach
  void setUp() {
    taskOccurrenceService =
        new TaskOccurrenceService(
            taskService,
            taskRepository,
            taskOccurrenceStateRepository,
            taskRecurrenceService,
            taskaProperties,
            taskOccurrenceNotificationService);
  }

  @Test
  void findOccurrence_generatedIdentity_returnsVirtualOccurrence() {
    Task series = recurringSeries();
    when(taskService.findById(series.getId())).thenReturn(series);
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            series.getId(), OCCURRENCE))
        .thenReturn(Optional.empty());
    when(taskRecurrenceService.getOccurrencesInRange(
            series,
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS),
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS).plusSeconds(86_400)))
        .thenReturn(List.of(OCCURRENCE));

    TaskResult result = taskOccurrenceService.findOccurrence(series.getId(), OCCURRENCE);

    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrence -> {
              assertThat(occurrence.virtual()).isTrue();
              assertThat(occurrence.detached()).isFalse();
            });
  }

  @Test
  void findOccurrence_detachedState_returnsPersistedOccurrenceWithoutRuleExpansion() {
    Task series = recurringSeries();
    TaskOccurrenceState detached = state(series.getId(), TaskOccurrenceStatus.MODIFIED);
    detached.setDetached(true);
    when(taskService.findById(series.getId())).thenReturn(series);
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            series.getId(), OCCURRENCE))
        .thenReturn(Optional.of(detached));

    TaskResult result = taskOccurrenceService.findOccurrence(series.getId(), OCCURRENCE);

    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrence -> {
              assertThat(occurrence.virtual()).isFalse();
              assertThat(occurrence.detached()).isTrue();
            });
    verify(taskRecurrenceService, never())
        .getOccurrencesInRange(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  void findOccurrence_skippedState_throwsNotFound() {
    Task series = recurringSeries();
    TaskOccurrenceState skipped = state(series.getId(), TaskOccurrenceStatus.SKIPPED);
    when(taskService.findById(series.getId())).thenReturn(series);
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            series.getId(), OCCURRENCE))
        .thenReturn(Optional.of(skipped));
    when(taskRecurrenceService.getOccurrencesInRange(
            series,
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS),
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS).plusSeconds(86_400)))
        .thenReturn(List.of(OCCURRENCE));

    assertThatThrownBy(() -> taskOccurrenceService.findOccurrence(series.getId(), OCCURRENCE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("skipped");
  }

  @Test
  void findOccurrence_unknownIdentity_throwsNotFound() {
    Task series = recurringSeries();
    when(taskService.findById(series.getId())).thenReturn(series);
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            series.getId(), OCCURRENCE))
        .thenReturn(Optional.empty());
    when(taskRecurrenceService.getOccurrencesInRange(
            series,
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS),
            OCCURRENCE.truncatedTo(java.time.temporal.ChronoUnit.DAYS).plusSeconds(86_400)))
        .thenReturn(List.of());

    assertThatThrownBy(() -> taskOccurrenceService.findOccurrence(series.getId(), OCCURRENCE))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  private Task recurringSeries() {
    Task task = new Task();
    task.setId(UUID.randomUUID());
    task.setContent("Recurring");
    task.setIsRecurring(true);
    task.setRecurrenceRule("FREQ=DAILY");
    task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    task.setLabels(List.of());
    return task;
  }

  private TaskOccurrenceState state(UUID seriesId, TaskOccurrenceStatus status) {
    TaskOccurrenceState occurrenceState = new TaskOccurrenceState();
    occurrenceState.setId(UUID.randomUUID());
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(OCCURRENCE);
    occurrenceState.setStatus(status);
    return occurrenceState;
  }
}
