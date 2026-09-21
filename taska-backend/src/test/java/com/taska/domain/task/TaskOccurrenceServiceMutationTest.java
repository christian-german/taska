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
import com.taska.domain.task.occurrence.repository.TaskOccurrenceState;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskOccurrenceService;
import com.taska.domain.task.occurrence.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Write operations on a single occurrence of a recurring series: overriding, skipping, completing
 * and reopening it. Each asserts what the occurrence state holds and that the series definition is
 * left untouched.
 *
 * <p>All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class TaskOccurrenceServiceMutationTest {

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
            taskRecurrenceService,
            taskaProperties,
            taskOccurrenceNotificationService);
  }

  @Test
  void replaceOccurrence_updatesOnlyItsSupportedOverrides() {
    UUID taskId = randomId();
    Instant occurrence = Instant.parse("2026-05-20T10:00:00Z");
    Task task = buildRecurringTask(taskId);
    TaskOccurrenceState existing =
        buildOccurrenceState(taskId, occurrence, TaskOccurrenceStatus.MODIFIED);
    existing.setTitle("Old title");
    existing.setPriority(4);
    existing.setScheduledAt(Instant.parse("2026-05-20T09:00:00Z"));
    existing.setDueAt(Instant.parse("2026-05-21T09:00:00Z"));
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrence));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(taskId, occurrence))
        .thenReturn(Optional.of(existing));
    when(taskOccurrenceStateRepository.save(existing)).thenReturn(existing);

    taskOccurrenceService.replaceOccurrence(
        taskId, occurrence, new TaskOccurrenceUpdateParameters(null));

    assertThat(task.getContent()).isEqualTo("Recurring task");
    assertThat(task.getRecurrenceRule()).isEqualTo("FREQ=DAILY");
    assertThat(existing.getTitle()).isEqualTo("Old title");
    assertThat(existing.getPriority()).isEqualTo(4);
    assertThat(existing.getScheduledAt()).isNull();
    assertThat(existing.getDueAt()).isEqualTo(Instant.parse("2026-05-21T09:00:00Z"));
    verify(taskOccurrenceNotificationService).clear(taskId, occurrence);
    verify(taskRepository, never()).save(any());
  }

  @Test
  void close_recurringOccurrenceVirtual_createsTaskOccurrenceStateWithStatusDone() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(TaskOccurrenceStatus.DONE);
    assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(saved.getSeriesId()).isEqualTo(taskId);
    assertThat(saved.getCompletedAt()).isNotNull();
  }

  @Test
  void close_sameOccurrenceTwice_throwsIllegalArgumentException() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState existingDone =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.DONE);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(existingDone));

    assertThatThrownBy(() -> taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void close_skippedOccurrence_throwsWithoutChangingItsState() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState skipped =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.SKIPPED);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(skipped));

    assertThatThrownBy(() -> taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("skipped");
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void close_modifiedOccurrence_preservesOverrides() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant movedScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
    TaskOccurrenceState modified =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    modified.setTitle("Overridden title");
    modified.setPriority(1);
    modified.setScheduledAt(movedScheduledAt);
    modified.setDueAt(Instant.parse("2026-05-20T15:00:00Z"));

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(modified));
    when(taskOccurrenceStateRepository.save(modified)).thenReturn(modified);

    taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt);

    assertThat(modified.getStatus()).isEqualTo(TaskOccurrenceStatus.DONE);
    assertThat(modified.getCompletedAt()).isNotNull();
    assertThat(modified.getTitle()).isEqualTo("Overridden title");
    assertThat(modified.getPriority()).isEqualTo(1);
    assertThat(modified.getScheduledAt()).isEqualTo(movedScheduledAt);
    assertThat(modified.getDueAt()).isEqualTo(Instant.parse("2026-05-20T15:00:00Z"));
  }

  @Test
  void close_recurringOccurrence_parentTaskNotSaved() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt);

    assertThat(task.getIsCompleted()).isFalse();
    assertThat(task.getCompletedAt()).isNull();
    verify(taskRepository, never()).save(any());
  }

  @Test
  void close_recurringTaskWithNullOccurrenceScheduledAt_throwsIllegalArgumentException() {
    UUID taskId = randomId();

    assertThatThrownBy(() -> taskOccurrenceService.closeOccurrence(taskId, null))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void reopen_completedUnmodifiedOccurrence_deletesStateAndReturnsVirtualOccurrence() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState completed =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.DONE);
    completed.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(completed));

    var result = taskOccurrenceService.reopenOccurrence(taskId, occurrenceScheduledAt);

    verify(taskOccurrenceStateRepository).delete(completed);
    verify(taskOccurrenceNotificationService, never()).clear(any(), any());
    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrenceResult -> assertThat(occurrenceResult.virtual()).isTrue());
  }

  @Test
  void reopen_completedModifiedOccurrence_preservesOverridesAndRestoresModifiedStatus() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant movedScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
    TaskOccurrenceState completed =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.DONE);
    completed.setTitle("Overridden title");
    completed.setPriority(1);
    completed.setScheduledAt(movedScheduledAt);
    completed.setDueAt(Instant.parse("2026-05-20T15:00:00Z"));
    completed.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(completed));
    when(taskOccurrenceStateRepository.save(completed)).thenReturn(completed);

    taskOccurrenceService.reopenOccurrence(taskId, occurrenceScheduledAt);

    assertThat(completed.getStatus()).isEqualTo(TaskOccurrenceStatus.MODIFIED);
    assertThat(completed.getCompletedAt()).isNull();
    assertThat(completed.getTitle()).isEqualTo("Overridden title");
    assertThat(completed.getPriority()).isEqualTo(1);
    assertThat(completed.getScheduledAt()).isEqualTo(movedScheduledAt);
    assertThat(completed.getDueAt()).isEqualTo(Instant.parse("2026-05-20T15:00:00Z"));
    verify(taskOccurrenceStateRepository).save(completed);
    verify(taskOccurrenceStateRepository, never()).delete(any());
    verify(taskOccurrenceNotificationService, never()).clear(any(), any());
  }

  @Test
  void reopen_modifiedOccurrence_throwsWithoutDeletingOverrides() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState modified =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    modified.setTitle("Override");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(modified));

    assertThatThrownBy(() -> taskOccurrenceService.reopenOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not completed");

    verify(taskOccurrenceStateRepository, never()).save(any());
    verify(taskOccurrenceStateRepository, never()).delete(any());
    assertThat(modified.getTitle()).isEqualTo("Override");
  }

  @Test
  void update_thisOnly_contentChange_isRejected() {
    Instant anchor = Instant.parse("2026-05-20T10:00:00Z");
    var request = taskRequest("New content", RecurrenceScope.THIS_ONLY, anchor);
    assertThatThrownBy(
            () ->
                taskOccurrenceService.updateOccurrence(
                    randomId(), anchor, request, request.priority() != null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Only scheduledAt");
    verifyNoInteractions(
        taskRepository, taskOccurrenceStateRepository, taskOccurrenceNotificationService);
  }

  @Test
  void update_thisOnly_scheduledAtChange_stateHasNewScheduledAtAndOriginalOccurrenceScheduledAt() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant newScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
    TaskPatchParameters request =
        reqWithScheduledAt(newScheduledAt, RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskOccurrenceService.updateOccurrence(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getScheduledAt()).isEqualTo(newScheduledAt);
    assertThat(saved.getOccurrenceScheduledAt())
        .isEqualTo(occurrenceScheduledAt); // original theoretical time preserved
    verify(taskOccurrenceNotificationService).clear(taskId, occurrenceScheduledAt);
  }

  @Test
  void update_thisOnly_priorityChange_isRejected() {
    Instant anchor = Instant.parse("2026-05-20T10:00:00Z");
    var request = taskRequest(null, 1, RecurrenceScope.THIS_ONLY, anchor);
    assertThatThrownBy(
            () ->
                taskOccurrenceService.updateOccurrence(
                    randomId(), anchor, request, request.priority() != null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Only scheduledAt");
    verifyNoInteractions(
        taskRepository, taskOccurrenceStateRepository, taskOccurrenceNotificationService);
  }

  @Test
  void update_thisOnly_parentTaskNotSaved() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        reqWithScheduledAt(
            occurrenceScheduledAt.plusSeconds(3600),
            RecurrenceScope.THIS_ONLY,
            occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    taskOccurrenceService.updateOccurrence(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

    assertThat(task.getContent()).isEqualTo("Recurring task"); // content unchanged
    verify(taskRepository, never()).save(any());
  }

  @Test
  void update_detachedOccurrence_usesPersistedIdentityWithoutExpandingTheRule() {
    UUID seriesId = randomId();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = buildRecurringTask(seriesId);
    TaskOccurrenceState detached =
        buildOccurrenceState(seriesId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    detached.setDetached(true);
    TaskPatchParameters parameters =
        reqWithScheduledAt(
            occurrenceScheduledAt.plusSeconds(3600),
            RecurrenceScope.THIS_ONLY,
            occurrenceScheduledAt);
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt))
        .thenReturn(Optional.of(detached));
    when(taskOccurrenceStateRepository.save(detached)).thenReturn(detached);

    TaskResult result =
        taskOccurrenceService.updateOccurrence(seriesId, occurrenceScheduledAt, parameters, false);

    assertThat(detached.getScheduledAt()).isEqualTo(occurrenceScheduledAt.plusSeconds(3600));
    assertThat(detached.isDetached()).isTrue();
    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrence -> assertThat(occurrence.detached()).isTrue());
    verifyNoInteractions(taskRecurrenceService);
  }

  @Test
  void replace_detachedOccurrence_preservesDetachedStateWithoutExpandingTheRule() {
    UUID seriesId = randomId();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = buildRecurringTask(seriesId);
    TaskOccurrenceState detached =
        buildOccurrenceState(seriesId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    detached.setDetached(true);
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt))
        .thenReturn(Optional.of(detached));
    when(taskOccurrenceStateRepository.save(detached)).thenReturn(detached);

    taskOccurrenceService.replaceOccurrence(
        seriesId, occurrenceScheduledAt, new TaskOccurrenceUpdateParameters(null));

    assertThat(detached.getTitle()).isNull();
    assertThat(detached.getPriority()).isNull();
    assertThat(detached.isDetached()).isTrue();
    verifyNoInteractions(taskRecurrenceService);
  }

  @Test
  void update_thisOnlyWithNullOccurrenceScheduledAt_throwsIllegalArgumentException() {
    UUID taskId = randomId();
    TaskPatchParameters request = taskRequest("Modified", RecurrenceScope.THIS_ONLY, null);

    assertThatThrownBy(
            () ->
                taskOccurrenceService.updateOccurrence(
                    taskId, null, request, request.priority() != null))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void delete_thisOnly_createsSkippedOccurrenceStateAndDoesNotDeleteTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskOccurrenceService.skipOccurrence(taskId, occurrenceScheduledAt);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(TaskOccurrenceStatus.SKIPPED);
    assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(saved.getSeriesId()).isEqualTo(taskId);
    verify(taskRepository, never()).delete(any());
  }

  @Test
  void delete_thisOnly_alreadySkipped_throwsIllegalArgumentException() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState existing =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.SKIPPED);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> taskOccurrenceService.skipOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void delete_thisOnly_occurrencePreviouslyDone_throwsIllegalStateException() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState existingDone =
        buildOccurrenceState(taskId, occurrenceScheduledAt, TaskOccurrenceStatus.DONE);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            taskId, occurrenceScheduledAt))
        .thenReturn(Optional.of(existingDone));

    assertThatThrownBy(() -> taskOccurrenceService.skipOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(IllegalStateException.class);
    verify(taskRepository, never()).delete(any());
  }

  @Test
  void delete_thisOnly_openDetachedOccurrence_removesItsStandaloneState() {
    UUID seriesId = randomId();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = buildRecurringTask(seriesId);
    TaskOccurrenceState detached =
        buildOccurrenceState(seriesId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    detached.setDetached(true);
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt))
        .thenReturn(Optional.of(detached));

    taskOccurrenceService.skipOccurrence(seriesId, occurrenceScheduledAt);

    verify(taskOccurrenceStateRepository).delete(detached);
    verify(taskOccurrenceStateRepository, never()).save(any());
    verifyNoInteractions(taskRecurrenceService);
  }

  @Test
  void close_detachedOccurrence_preservesItsStandaloneIdentity() {
    UUID seriesId = randomId();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = buildRecurringTask(seriesId);
    TaskOccurrenceState detached =
        buildOccurrenceState(seriesId, occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    detached.setDetached(true);
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt))
        .thenReturn(Optional.of(detached));
    when(taskOccurrenceStateRepository.save(detached)).thenReturn(detached);

    TaskResult result = taskOccurrenceService.closeOccurrence(seriesId, occurrenceScheduledAt);

    assertThat(detached.getStatus()).isEqualTo(TaskOccurrenceStatus.DONE);
    assertThat(detached.getCompletedAt()).isNotNull();
    assertThat(detached.isDetached()).isTrue();
    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrence -> assertThat(occurrence.detached()).isTrue());
    verifyNoInteractions(taskRecurrenceService);
  }

  @Test
  void reopen_detachedCompletionWithoutOverrides_keepsMaterializedDetachedState() {
    UUID seriesId = randomId();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = buildRecurringTask(seriesId);
    TaskOccurrenceState detached =
        buildOccurrenceState(seriesId, occurrenceScheduledAt, TaskOccurrenceStatus.DONE);
    detached.setDetached(true);
    detached.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt))
        .thenReturn(Optional.of(detached));
    when(taskOccurrenceStateRepository.save(detached)).thenReturn(detached);

    TaskResult result = taskOccurrenceService.reopenOccurrence(seriesId, occurrenceScheduledAt);

    assertThat(detached.getStatus()).isEqualTo(TaskOccurrenceStatus.MODIFIED);
    assertThat(detached.getCompletedAt()).isNull();
    assertThat(detached.isDetached()).isTrue();
    assertThat(result)
        .isInstanceOfSatisfying(
            RecurringTaskOccurrenceResult.class,
            occurrence -> {
              assertThat(occurrence.virtual()).isFalse();
              assertThat(occurrence.detached()).isTrue();
              assertThat(occurrence.completed()).isFalse();
            });
    verify(taskOccurrenceStateRepository, never()).delete(any());
    verifyNoInteractions(taskRecurrenceService);
  }

  @Test
  void close_occurrenceScheduledAtNotMatchingAnyRealOccurrence_throwsResourceNotFoundException() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    // This timestamp is not a valid FREQ=DAILY occurrence at 10:00
    Instant nonExistent = Instant.parse("2026-05-20T13:37:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    // getOccurrencesInRange returns the real occurrences (10:00), not the bogus one
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(Instant.parse("2026-05-20T10:00:00Z")));

    assertThatThrownBy(() -> taskOccurrenceService.closeOccurrence(taskId, nonExistent))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void close_concurrentDuplicateSave_dataIntegrityViolationPropagates() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.save(any()))
        .thenThrow(
            new DataIntegrityViolationException("duplicate key value violates unique constraint"));

    assertThatThrownBy(() -> taskOccurrenceService.closeOccurrence(taskId, occurrenceScheduledAt))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void detachStatesFrom_keepsCompletionsAndOverridesButDropsSkips() {
    UUID seriesId = randomId();
    Instant cut = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState done = buildOccurrenceState(seriesId, cut, TaskOccurrenceStatus.DONE);
    TaskOccurrenceState modified =
        buildOccurrenceState(
            seriesId, Instant.parse("2026-05-21T10:00:00Z"), TaskOccurrenceStatus.MODIFIED);
    TaskOccurrenceState skipped =
        buildOccurrenceState(
            seriesId, Instant.parse("2026-05-22T10:00:00Z"), TaskOccurrenceStatus.SKIPPED);
    when(taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAtGreaterThanEqual(
            seriesId, cut))
        .thenReturn(List.of(done, modified, skipped));

    taskOccurrenceService.detachStatesFrom(seriesId, cut);

    // A completion and a deliberate override survive on their own date; a skip has lost its object.
    assertThat(done.isDetached()).isTrue();
    assertThat(modified.isDetached()).isTrue();
    verify(taskOccurrenceStateRepository).save(done);
    verify(taskOccurrenceStateRepository).save(modified);
    verify(taskOccurrenceStateRepository).delete(skipped);
    verify(taskOccurrenceStateRepository, never()).save(skipped);
  }
}
