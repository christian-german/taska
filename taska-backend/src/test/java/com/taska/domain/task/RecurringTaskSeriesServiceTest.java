package com.taska.domain.task;

import static com.taska.domain.task.TaskMutationFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.taska.config.TaskaProperties;
import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.repository.TaskRepository;
import com.taska.domain.task.definition.service.TaskDefinitionService;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskOccurrenceService;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.series.service.RecurringTaskSeriesService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Series stopping validates the cut and preserves history without creating another series.
 *
 * <p>All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class RecurringTaskSeriesServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private TaskRecurrenceService taskRecurrenceService;
  @Mock private ProjectRepository projectRepository;
  @Mock private TaskPriorityEvaluationRepository priorityEvaluationRepository;
  @Mock private PlanningCalendarService planningCalendarService;
  @Mock private TaskOccurrenceNotificationService taskOccurrenceNotificationService;
  @Mock private TaskaProperties taskaProperties;

  private TaskDefinitionService taskService;
  private TaskOccurrenceService taskOccurrenceService;
  private RecurringTaskSeriesService recurringTaskSeriesService;

  @BeforeEach
  void createServicesUnderTest() {
    taskService =
        new TaskDefinitionService(
            taskRepository,
            projectRepository,
            priorityEvaluationRepository,
            planningCalendarService);

    taskOccurrenceService =
        new TaskOccurrenceService(
            taskService,
            taskRepository,
            taskOccurrenceStateRepository,
            new TaskRecurrenceService(),
            taskaProperties,
            taskOccurrenceNotificationService);

    recurringTaskSeriesService =
        new RecurringTaskSeriesService(taskService, taskOccurrenceService, taskRepository);
  }

  @Test
  void stoppingPreservesMovedAndCompletedHistoryWithoutCreatingASuccessor() {
    UUID id = randomId();
    Task series = buildRecurringTask(id);
    Instant cut = Instant.parse("2026-05-20T10:00:00Z");
    var moved = buildOccurrenceState(id, cut, TaskOccurrenceStatus.MODIFIED);
    moved.setScheduledAt(cut.plusSeconds(3600));
    var done = buildOccurrenceState(id, cut.plusSeconds(86400), TaskOccurrenceStatus.DONE);
    var skipped = buildOccurrenceState(id, cut.plusSeconds(172800), TaskOccurrenceStatus.SKIPPED);
    when(taskRepository.findById(id)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAtGreaterThanEqual(
            id, cut))
        .thenReturn(List.of(moved, done, skipped));

    recurringTaskSeriesService.truncateSeriesFrom(id, cut);

    assertThat(series.getRruleEndsAt()).isEqualTo(cut.minusSeconds(1));
    assertThat(moved.isDetached()).isTrue();
    assertThat(done.isDetached()).isTrue();
    assertThat(moved.getScheduledAt()).isEqualTo(cut.plusSeconds(3600));
    verify(taskOccurrenceStateRepository).delete(skipped);
    verify(taskRepository).save(series);
    verify(taskRepository, times(1)).save(any());
    verify(taskRepository, never()).delete(any());
  }

  @Test
  void stoppingRejectsAnInvalidIdentityWithoutMutations() {
    UUID id = randomId();
    Task series = buildRecurringTask(id);
    when(taskRepository.findById(id)).thenReturn(Optional.of(series));

    assertThatThrownBy(
            () ->
                recurringTaskSeriesService.truncateSeriesFrom(
                    id, Instant.parse("2026-05-20T11:00:00Z")))
        .isInstanceOf(com.taska.exception.ResourceNotFoundException.class);
    assertThat(series.getRruleEndsAt()).isNull();
    verify(taskRepository, never()).save(any());
    verifyNoInteractions(taskOccurrenceStateRepository);
  }

  @Test
  void stoppingCannotExtendAnExistingEnd() {
    UUID id = randomId();
    Task series = buildRecurringTask(id);
    Instant end = Instant.parse("2026-05-20T09:59:59Z");
    series.setRruleEndsAt(end);
    when(taskRepository.findById(id)).thenReturn(Optional.of(series));

    assertThatThrownBy(
            () ->
                recurringTaskSeriesService.truncateSeriesFrom(
                    id, Instant.parse("2026-05-21T10:00:00Z")))
        .isInstanceOf(com.taska.exception.ResourceNotFoundException.class);
    assertThat(series.getRruleEndsAt()).isEqualTo(end);
    verify(taskRepository, never()).save(any());
  }

  @Test
  void stoppingAtFirstOccurrenceProducesNoFutureOccurrences() {
    UUID id = randomId();
    Task series = buildRecurringTask(id);
    when(taskRepository.findById(id)).thenReturn(Optional.of(series));

    recurringTaskSeriesService.truncateSeriesFrom(id, series.getScheduledAt());

    assertThat(
            new TaskRecurrenceService()
                .getOccurrencesInRange(
                    series, series.getScheduledAt(), series.getScheduledAt().plusSeconds(86400)))
        .isEmpty();
  }
}
