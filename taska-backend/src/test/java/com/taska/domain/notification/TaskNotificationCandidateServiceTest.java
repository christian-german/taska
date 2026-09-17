package com.taska.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.service.TaskNotificationCandidateService;
import com.taska.domain.task.occurrence.TaskOccurrenceState;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskNotificationCandidateServiceTest {

  private static final Instant WINDOW_START = Instant.parse("2026-05-20T10:00:00Z");
  private static final Instant WINDOW_END = Instant.parse("2026-05-20T10:15:00Z");

  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private TaskRecurrenceService taskRecurrenceService;
  @InjectMocks private TaskNotificationCandidateService service;

  @BeforeEach
  void noNonRecurringCandidates() {
    when(taskRepository.findTasksDueAround(WINDOW_END)).thenReturn(List.of());
  }

  @Test
  void virtualOccurrenceInTheWindowIsEligibleWithoutMaterializingIt() {
    Task task = recurringTask(false);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:10:00Z");
    when(taskRepository.findActiveRecurringTasksForPeriod(WINDOW_START, WINDOW_END))
        .thenReturn(List.of(task));
    when(taskOccurrenceStateRepository.findByStatusAndScheduledAtBetween(
            TaskOccurrenceStatus.MODIFIED, WINDOW_START, WINDOW_END))
        .thenReturn(List.of());
    when(taskOccurrenceStateRepository.findBySeriesIdInAndOccurrenceScheduledAtBetween(
            List.of(task.getId()), WINDOW_START, WINDOW_END))
        .thenReturn(List.of());
    when(taskRecurrenceService.getOccurrencesInRange(task, WINDOW_START, WINDOW_END))
        .thenReturn(List.of(occurrenceScheduledAt));

    assertThat(service.findEligible(WINDOW_START, WINDOW_END))
        .singleElement()
        .satisfies(
            candidate -> {
              assertThat(candidate.content()).isEqualTo("Recurring task");
              assertThat(candidate.scheduledAt()).isEqualTo(occurrenceScheduledAt);
              assertThat(candidate.occurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
            });
  }

  @Test
  void modifiedOccurrenceMovedIntoTheWindowUsesItsEffectiveValues() {
    Task task = recurringTask(false);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-19T10:00:00Z");
    Instant movedScheduledAt = Instant.parse("2026-05-20T10:05:00Z");
    TaskOccurrenceState occurrenceState =
        occurrenceState(task, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    occurrenceState.setTitle("Moved occurrence");
    occurrenceState.setScheduledAt(movedScheduledAt);
    when(taskRepository.findActiveRecurringTasksForPeriod(WINDOW_START, WINDOW_END))
        .thenReturn(List.of());
    when(taskRepository.findAllById(Set.of(task.getId()))).thenReturn(List.of(task));
    when(taskOccurrenceStateRepository.findByStatusAndScheduledAtBetween(
            TaskOccurrenceStatus.MODIFIED, WINDOW_START, WINDOW_END))
        .thenReturn(List.of(occurrenceState));
    when(taskOccurrenceStateRepository.findBySeriesIdInAndOccurrenceScheduledAtBetween(
            List.of(task.getId()), WINDOW_START, WINDOW_END))
        .thenReturn(List.of());
    when(taskRecurrenceService.getOccurrencesInRange(task, WINDOW_START, WINDOW_END))
        .thenReturn(List.of());

    assertThat(service.findEligible(WINDOW_START, WINDOW_END))
        .singleElement()
        .satisfies(
            candidate -> {
              assertThat(candidate.content()).isEqualTo("Moved occurrence");
              assertThat(candidate.scheduledAt()).isEqualTo(movedScheduledAt);
              assertThat(candidate.occurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
            });
  }

  @Test
  void completedAndSkippedOccurrencesAreNotEligible() {
    Task task = recurringTask(false);
    Instant completedAt = Instant.parse("2026-05-20T10:05:00Z");
    Instant skippedAt = Instant.parse("2026-05-20T10:10:00Z");
    when(taskRepository.findActiveRecurringTasksForPeriod(WINDOW_START, WINDOW_END))
        .thenReturn(List.of(task));
    when(taskOccurrenceStateRepository.findByStatusAndScheduledAtBetween(
            TaskOccurrenceStatus.MODIFIED, WINDOW_START, WINDOW_END))
        .thenReturn(List.of());
    when(taskOccurrenceStateRepository.findBySeriesIdInAndOccurrenceScheduledAtBetween(
            List.of(task.getId()), WINDOW_START, WINDOW_END))
        .thenReturn(
            List.of(
                occurrenceState(task, completedAt, TaskOccurrenceStatus.DONE),
                occurrenceState(task, skippedAt, TaskOccurrenceStatus.SKIPPED)));
    when(taskRecurrenceService.getOccurrencesInRange(task, WINDOW_START, WINDOW_END))
        .thenReturn(List.of(completedAt, skippedAt));

    assertThat(service.findEligible(WINDOW_START, WINDOW_END)).isEmpty();
  }

  @Test
  void allDayRecurringTaskIsNotExpanded() {
    Task task = recurringTask(true);
    when(taskRepository.findActiveRecurringTasksForPeriod(WINDOW_START, WINDOW_END))
        .thenReturn(List.of(task));
    when(taskOccurrenceStateRepository.findByStatusAndScheduledAtBetween(
            TaskOccurrenceStatus.MODIFIED, WINDOW_START, WINDOW_END))
        .thenReturn(List.of());

    assertThat(service.findEligible(WINDOW_START, WINDOW_END)).isEmpty();
    verify(taskRecurrenceService, never()).getOccurrencesInRange(task, WINDOW_START, WINDOW_END);
  }

  private Task recurringTask(boolean allDay) {
    Task task = new Task();
    task.setId(UUID.randomUUID());
    task.setContent("Recurring task");
    task.setDescription("Description");
    task.setIsRecurring(true);
    task.setRecurrenceRule("FREQ=DAILY");
    task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    task.setAllDay(allDay);
    return task;
  }

  private TaskOccurrenceState occurrenceState(
      Task task, Instant occurrenceScheduledAt, TaskOccurrenceStatus status) {
    TaskOccurrenceState occurrenceState = new TaskOccurrenceState();
    occurrenceState.setSeriesId(task.getId());
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(status);
    return occurrenceState;
  }
}
