package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.Project;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskService;
import com.taska.domain.task.service.TaskUpdateParameters;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Unit tests for TaskService write operations: close, reopen, update, delete. All repository calls
 * are mocked. Tests focus on what gets persisted and whether the parent task is left untouched when
 * only an instance should change.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceMutationTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private TaskRecurrenceService taskRecurrenceService;

  @Mock private ProjectRepository projectRepository;
  @Mock private TaskPriorityEvaluationRepository priorityEvaluationRepository;
  @Mock private PlanningCalendarService planningCalendarService;
  @Mock private TaskOccurrenceNotificationService taskOccurrenceNotificationService;

  @InjectMocks private TaskService taskService;

  // ── Helpers ──────────────────────────────────────────────────────────────

  private UUID randomId() {
    return UUID.randomUUID();
  }

  private Task buildNonRecurringTask(UUID id) {
    Task task = new Task();
    task.setId(id);
    task.setContent("Non-recurring task");
    task.setIsRecurring(false);
    task.setIsCompleted(false);
    task.setLabels(List.of());
    task.setPriority(4);
    return task;
  }

  private Task buildRecurringTask(UUID id) {
    Task task = new Task();
    task.setId(id);
    task.setContent("Recurring task");
    task.setIsRecurring(true);
    task.setRecurrenceRule("FREQ=DAILY");
    task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    task.setIsCompleted(false);
    task.setLabels(List.of());
    task.setPriority(4);
    return task;
  }

  private TaskOccurrenceState buildOccurrenceState(
      UUID seriesId, Instant occurrenceScheduledAt, TaskOccurrenceStatus status) {
    TaskOccurrenceState occurrenceState = new TaskOccurrenceState();
    occurrenceState.setId(randomId());
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(status);
    return occurrenceState;
  }

  private TaskUpdateParameters replacementRequest(UUID projectId, UUID parentId) {
    return new TaskUpdateParameters(
        "Replacement",
        TaskType.APPOINTMENT,
        null,
        projectId,
        parentId,
        7,
        null,
        List.of("important"),
        null,
        null,
        true,
        false,
        null,
        null,
        null);
  }

  /** Minimal task patch with only the fields required for the test. All unused fields are null. */
  private TaskPatchParameters taskRequest(
      String content, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  private TaskPatchParameters taskRequest(
      String content, Integer priority, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        priority,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  private TaskPatchParameters reqWithScheduledAt(
      Instant scheduledAt, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scheduledAt,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  private TaskPatchParameters reqWithRRule(
      String content, String recurrenceRule, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        recurrenceRule,
        scope,
        occurrenceScheduledAt,
        null);
  }

  private TaskCreateParameters createParameters(String content, TaskType type) {
    return new TaskCreateParameters(
        content, null, null, null, 0, null, null, null, null, false, false, null, null, null, type);
  }

  @Test
  void create_withoutManualPriority_persistsNullRatherThanNormalPriority() {
    UUID inboxId = randomId();
    Project inbox = new Project();
    inbox.setId(inboxId);
    when(projectRepository.findByIsInboxProjectTrue()).thenReturn(Optional.of(inbox));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task created = taskService.create(createParameters("Unprioritized", TaskType.TODO));

    assertThat(created.getPriority()).isNull();
    assertThat(created.getProjectId()).isEqualTo(inboxId);
  }

  @Test
  void update_withExplicitNullPriority_clearsManualPriority() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.update(taskId, taskRequest(null, null, null), true);

    assertThat(task.getPriority()).isNull();
  }

  @Test
  void update_dueAtChangesDeadlineWithoutChangingScheduledAt() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    Instant scheduledAt = Instant.parse("2026-05-20T09:00:00Z");
    Instant dueAt = Instant.parse("2026-05-21T17:00:00Z");
    task.setScheduledAt(scheduledAt);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.update(
        taskId,
        new TaskPatchParameters(
            null, null, null, null, null, null, null, null, dueAt, null, null, null, null, null,
            null, null, null),
        false);

    assertThat(task.getDueAt()).isEqualTo(dueAt);
    assertThat(task.getScheduledAt()).isEqualTo(scheduledAt);
  }

  @Test
  void replace_replacesEveryMutableFieldAndRetainsOutputOnlyState() {
    UUID taskId = randomId();
    UUID projectId = randomId();
    UUID parentId = randomId();
    Task task = buildNonRecurringTask(taskId);
    task.setDescription("Old description");
    task.setProjectId(randomId());
    task.setParentId(randomId());
    task.setPosition(1);
    task.setPriority(1);
    task.setLabels(List.of("old"));
    task.setScheduledAt(Instant.parse("2026-05-20T09:00:00Z"));
    task.setDueAt(Instant.parse("2026-05-21T09:00:00Z"));
    task.setEstimateMinutes(60);
    task.setMentionContext("old context");
    task.setRecurrenceRule("FREQ=DAILY");
    task.setIsCompleted(true);
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
    task.setCreatedAt(createdAt);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.replace(taskId, replacementRequest(projectId, parentId));

    assertThat(task.getId()).isEqualTo(taskId);
    assertThat(task.getIsCompleted()).isTrue();
    assertThat(task.getCreatedAt()).isEqualTo(createdAt);
    assertThat(task.getContent()).isEqualTo("Replacement");
    assertThat(task.getType()).isEqualTo(TaskType.APPOINTMENT);
    assertThat(task.getDescription()).isNull();
    assertThat(task.getProjectId()).isEqualTo(projectId);
    assertThat(task.getParentId()).isEqualTo(parentId);
    assertThat(task.getPosition()).isEqualTo(7);
    assertThat(task.getPriority()).isNull();
    assertThat(task.getLabels()).containsExactly("important");
    assertThat(task.getScheduledAt()).isNull();
    assertThat(task.getDueAt()).isNull();
    assertThat(task.isAllDay()).isTrue();
    assertThat(task.getIsRecurring()).isFalse();
    assertThat(task.getEstimateMinutes()).isNull();
    assertThat(task.getMentionContext()).isNull();
    assertThat(task.getRecurrenceRule()).isNull();
    verify(priorityEvaluationRepository).deleteByTaskId(taskId);
  }

  @Test
  void replaceFollowing_createsACompleteReplacementSeries() {
    UUID taskId = randomId();
    UUID projectId = randomId();
    Instant occurrence = Instant.parse("2026-05-20T10:00:00Z");
    Task original = buildRecurringTask(taskId);
    TaskUpdateParameters request =
        new TaskUpdateParameters(
            "Following",
            TaskType.APPOINTMENT,
            "New description",
            projectId,
            null,
            4,
            2,
            List.of("next"),
            occurrence,
            null,
            false,
            true,
            45,
            "context",
            "FREQ=WEEKLY");
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(original));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrence));
    Project project = new Project();
    project.setId(projectId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(planningCalendarService.allows(any(), eq(occurrence), eq(false))).thenReturn(true);
    ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(saved.capture())).thenAnswer(invocation -> invocation.getArgument(0));

    taskService.replaceFollowing(taskId, occurrence, request);

    List<Task> persisted = saved.getAllValues();
    assertThat(persisted).hasSize(2);
    assertThat(persisted.getFirst().getRruleEndsAt())
        .isEqualTo(occurrence.minus(1, ChronoUnit.SECONDS));
    Task replacement = persisted.get(1);
    assertThat(replacement.getContent()).isEqualTo("Following");
    assertThat(replacement.getType()).isEqualTo(TaskType.APPOINTMENT);
    assertThat(replacement.getDescription()).isEqualTo("New description");
    assertThat(replacement.getProjectId()).isEqualTo(projectId);
    assertThat(replacement.getPosition()).isEqualTo(4);
    assertThat(replacement.getPriority()).isEqualTo(2);
    assertThat(replacement.getLabels()).containsExactly("next");
    assertThat(replacement.getScheduledAt()).isEqualTo(occurrence);
    assertThat(replacement.getDueAt()).isNull();
    assertThat(replacement.isAllDay()).isFalse();
    assertThat(replacement.getIsRecurring()).isTrue();
    assertThat(replacement.getEstimateMinutes()).isEqualTo(45);
    assertThat(replacement.getMentionContext()).isEqualTo("context");
    assertThat(replacement.getRecurrenceRule()).isEqualTo("FREQ=WEEKLY");
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

    taskService.replaceOccurrence(
        taskId, occurrence, new TaskOccurrenceUpdateParameters("Occurrence", null, null, null));

    assertThat(task.getContent()).isEqualTo("Recurring task");
    assertThat(task.getRecurrenceRule()).isEqualTo("FREQ=DAILY");
    assertThat(existing.getTitle()).isEqualTo("Occurrence");
    assertThat(existing.getPriority()).isNull();
    assertThat(existing.getScheduledAt()).isNull();
    assertThat(existing.getDueAt()).isNull();
    verify(taskOccurrenceNotificationService).clear(taskId, occurrence);
    verify(taskRepository, never()).save(any());
  }

  // ═════════════════════════════════════════════════════════════════════════
  // close() — section 3
  // ═════════════════════════════════════════════════════════════════════════

  // ── 3.1 ──────────────────────────────────────────────────────────────────

  @Test
  void close_nonRecurringTask_setsIsCompletedTrueAndCompletedAtOnTask() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.close(taskId, null);

    assertThat(task.getIsCompleted()).isTrue();
    assertThat(task.getCompletedAt()).isNotNull();
    verify(taskRepository).save(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 3.2 ──────────────────────────────────────────────────────────────────

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

    taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt));

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(TaskOccurrenceStatus.DONE);
    assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(saved.getSeriesId()).isEqualTo(taskId);
    assertThat(saved.getCompletedAt()).isNotNull();
  }

  // ── 3.3 ──────────────────────────────────────────────────────────────────
  // Calling close() on an already-completed occurrence throws IllegalArgumentException.
  // Idempotent re-close is not supported at the service layer.

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

    assertThatThrownBy(
            () -> taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt)))
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

    assertThatThrownBy(
            () -> taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt)))
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

    taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt));

    assertThat(modified.getStatus()).isEqualTo(TaskOccurrenceStatus.DONE);
    assertThat(modified.getCompletedAt()).isNotNull();
    assertThat(modified.getTitle()).isEqualTo("Overridden title");
    assertThat(modified.getPriority()).isEqualTo(1);
    assertThat(modified.getScheduledAt()).isEqualTo(movedScheduledAt);
    assertThat(modified.getDueAt()).isEqualTo(Instant.parse("2026-05-20T15:00:00Z"));
  }

  // ── 3.4 ──────────────────────────────────────────────────────────────────

  @Test
  void close_recurringOccurrence_parentTaskNotSaved() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt));

    assertThat(task.getIsCompleted()).isFalse();
    assertThat(task.getCompletedAt()).isNull();
    verify(taskRepository, never()).save(any());
  }

  // ── 3.5 ──────────────────────────────────────────────────────────────────
  // When occurrenceScheduledAt is absent for a recurring task, the service throws
  // IllegalArgumentException: recurring occurrences always require a occurrenceScheduledAt.

  @Test
  void close_recurringTaskWithNullOccurrenceScheduledAt_throwsIllegalArgumentException() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> taskService.close(taskId, new TaskCloseReopenParameters(null)))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 3.6 ──────────────────────────────────────────────────────────────────

  @Test
  void reopen_nonRecurringTask_setsIsCompletedFalseAndClearsCompletedAt() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    task.setIsCompleted(true);
    task.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.reopen(taskId, null);

    assertThat(task.getIsCompleted()).isFalse();
    assertThat(task.getCompletedAt()).isNull();
    verify(taskRepository).save(task);
  }

  // ── 3.7 ──────────────────────────────────────────────────────────────────

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

    var result = taskService.reopen(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt));

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

    taskService.reopen(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt));

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
  void reopen_recurringTaskWithoutOccurrence_throwsWithoutSavingSeries() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> taskService.reopen(taskId, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("occurrenceScheduledAt");

    verify(taskRepository, never()).save(any());
    assertThat(task.getIsCompleted()).isFalse();
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

    assertThatThrownBy(
            () -> taskService.reopen(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not completed");

    verify(taskOccurrenceStateRepository, never()).save(any());
    verify(taskOccurrenceStateRepository, never()).delete(any());
    assertThat(modified.getTitle()).isEqualTo("Override");
  }

  // ═════════════════════════════════════════════════════════════════════════
  // update() — section 4
  // ═════════════════════════════════════════════════════════════════════════

  // ── 4.1 ──────────────────────────────────────────────────────────────────

  @Test
  void update_nonRecurringTask_patchesTaskDirectlyNoInstanceCreated() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    TaskPatchParameters request = taskRequest("Updated content", null, null);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.update(taskId, request, request.priority() != null);

    assertThat(task.getContent()).isEqualTo("Updated content");
    verify(taskRepository).save(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 4.2 ──────────────────────────────────────────────────────────────────

  @Test
  void update_thisOnly_contentChange_createsModifiedInstanceWithTitle() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("New content", RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(TaskOccurrenceStatus.MODIFIED);
    assertThat(saved.getTitle()).isEqualTo("New content");
    assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(saved.getSeriesId()).isEqualTo(taskId);
  }

  // ── 4.3 ──────────────────────────────────────────────────────────────────

  @Test
  void
      update_thisOnly_scheduledAtChange_instanceHasNewScheduledAtAndOriginalOccurrenceScheduledAt() {
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

    taskService.update(taskId, request, request.priority() != null);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getScheduledAt()).isEqualTo(newScheduledAt);
    assertThat(saved.getOccurrenceScheduledAt())
        .isEqualTo(occurrenceScheduledAt); // original theoretical time preserved
    verify(taskOccurrenceNotificationService).clear(taskId, occurrenceScheduledAt);
  }

  // ── 4.4 ──────────────────────────────────────────────────────────────────

  @Test
  void update_thisOnly_priorityChange_instanceHasNewPriority() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("Recurring task", 1, RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getPriority()).isEqualTo(1);
  }

  // ── 4.5 ──────────────────────────────────────────────────────────────────

  @Test
  void update_thisOnly_parentTaskNotSaved() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("New content", RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    when(taskOccurrenceStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    assertThat(task.getContent()).isEqualTo("Recurring task"); // content unchanged
    verify(taskRepository, never()).save(any());
  }

  // ── 4.6 ──────────────────────────────────────────────────────────────────

  @Test
  void update_fromThis_contentChange_truncatesOriginalAndClonesTaskWithNewContent() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("New content", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    List<Task> savedTasks = taskCaptor.getAllValues();
    assertThat(savedTasks).hasSize(2);

    Task savedOriginal = savedTasks.get(0);
    assertThat(savedOriginal.getRruleEndsAt())
        .isEqualTo(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));

    Task clone = savedTasks.get(1);
    assertThat(clone.getContent()).isEqualTo("New content");
    assertThat(clone.getScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(clone.getIsRecurring()).isTrue();
  }

  // ── 4.7 ──────────────────────────────────────────────────────────────────

  @Test
  void update_fromThis_cloneInheritsProjectIdAndParentId() {
    UUID taskId = randomId();
    UUID projectId = randomId();
    UUID parentId = randomId();
    Task task = buildRecurringTask(taskId);
    task.setProjectId(projectId);
    task.setParentId(parentId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("Updated", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    Task clone = taskCaptor.getAllValues().get(1);
    assertThat(clone.getProjectId()).isEqualTo(projectId);
    assertThat(clone.getParentId()).isEqualTo(parentId);
  }

  // ── 4.8 ──────────────────────────────────────────────────────────────────

  @Test
  void update_fromThis_firstOccurrence_originalRruleEndsAtIsOneDayBeforeScheduledAt() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant firstOccurrence =
        task.getScheduledAt(); // occurrenceScheduledAt == scheduledAt for the first occurrence
    TaskPatchParameters request =
        taskRequest("Updated", RecurrenceScope.FROM_THIS, firstOccurrence);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    Task savedOriginal = taskCaptor.getAllValues().getFirst();
    assertThat(savedOriginal.getRruleEndsAt())
        .isEqualTo(firstOccurrence.minus(1, ChronoUnit.SECONDS));
  }

  // ── 4.9 ──────────────────────────────────────────────────────────────────
  // With scope=THIS_ONLY and occurrenceScheduledAt=null, the service throws
  // IllegalArgumentException: occurrenceScheduledAt is required whenever a scope is set.

  @Test
  void update_thisOnlyWithNullOccurrenceScheduledAt_throwsIllegalArgumentException() {
    UUID taskId = randomId();
    TaskPatchParameters request = taskRequest("Modified", RecurrenceScope.THIS_ONLY, null);

    assertThatThrownBy(() -> taskService.update(taskId, request, request.priority() != null))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 4.10 ─────────────────────────────────────────────────────────────────

  @Test
  void update_fromThis_newRecurrenceRule_cloneHasNewRuleOriginalKeepsOldRule() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    String originalRule = task.getRecurrenceRule(); // FREQ=DAILY
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        reqWithRRule("Updated", "FREQ=WEEKLY", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    List<Task> saved = taskCaptor.getAllValues();
    assertThat(saved.get(0).getRecurrenceRule()).isEqualTo(originalRule); // original unchanged
    assertThat(saved.get(1).getRecurrenceRule()).isEqualTo("FREQ=WEEKLY");
  }

  // ── 4.6 (rruleEndsAt null on clone — see also 6.6) ───────────────────────

  @Test
  void update_fromThis_cloneHasNullRruleEndsAtEvenIfOriginalHadOne() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    task.setRruleEndsAt(Instant.parse("2026-12-31T23:59:59Z"));
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskPatchParameters request =
        taskRequest("Updated", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

    taskService.update(taskId, request, request.priority() != null);

    Task clone = taskCaptor.getAllValues().get(1);
    assertThat(clone.getRruleEndsAt()).isNull();
  }

  // ═════════════════════════════════════════════════════════════════════════
  // delete() — section 5
  // ═════════════════════════════════════════════════════════════════════════

  // ── 5.1 ──────────────────────────────────────────────────────────────────

  @Test
  void delete_nonRecurringTaskNullBody_physicallyDeletesTask() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.delete(taskId, null);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 5.2 ──────────────────────────────────────────────────────────────────

  @Test
  void delete_recurringTaskNullBody_physicallyDeletesTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.delete(taskId, null);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 5.3 ──────────────────────────────────────────────────────────────────

  @Test
  void delete_thisOnly_createsSkippedInstanceAndDoesNotDeleteTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRecurrenceService.getOccurrencesInRange(any(), any(), any()))
        .thenReturn(List.of(occurrenceScheduledAt));
    ArgumentCaptor<TaskOccurrenceState> captor = ArgumentCaptor.forClass(TaskOccurrenceState.class);
    when(taskOccurrenceStateRepository.save(captor.capture()))
        .thenAnswer(inv -> inv.getArgument(0));

    taskService.delete(
        taskId, new TaskDeleteParameters(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt));

    TaskOccurrenceState saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(TaskOccurrenceStatus.SKIPPED);
    assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    assertThat(saved.getSeriesId()).isEqualTo(taskId);
    verify(taskRepository, never()).delete(any());
  }

  // ── 5.4 ──────────────────────────────────────────────────────────────────
  // Deleting an already-SKIPPED occurrence throws IllegalArgumentException.
  // Double-skip is not allowed; the client must check the occurrence state first.

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

    assertThatThrownBy(
            () ->
                taskService.delete(
                    taskId,
                    new TaskDeleteParameters(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ── 5.5 ──────────────────────────────────────────────────────────────────

  @Test
  void delete_fromThis_setsRruleEndsAtAndDoesNotDeleteTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.delete(
        taskId, new TaskDeleteParameters(RecurrenceScope.FROM_THIS, occurrenceScheduledAt));

    assertThat(task.getRruleEndsAt()).isEqualTo(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    verify(taskRepository).save(task);
    verify(taskRepository, never()).delete(any());
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 5.6 ──────────────────────────────────────────────────────────────────

  @Test
  void delete_fromThis_firstOccurrence_rruleEndsAtIsOneDayBeforeScheduledAt() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant firstOccurrence = task.getScheduledAt();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.delete(
        taskId, new TaskDeleteParameters(RecurrenceScope.FROM_THIS, firstOccurrence));

    assertThat(task.getRruleEndsAt()).isEqualTo(firstOccurrence.minus(1, ChronoUnit.SECONDS));
  }

  // ── 5.7 ──────────────────────────────────────────────────────────────────
  // Cascade deletion of task_occurrence_states is enforced at the DB level (FK CASCADE).
  // At the service level we verify that taskRepository.delete is called.

  @Test
  void delete_parentTask_callsRepositoryDeleteAndNoDirectInstanceDeletion() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.delete(taskId, null);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never())
        .deleteBySeriesIdAndOccurrenceScheduledAt(any(), any());
  }

  // ── 5.8 ──────────────────────────────────────────────────────────────────
  // THIS_ONLY on a DONE occurrence throws IllegalStateException.
  // The occurrence must be reopened before it can be skipped.

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

    assertThatThrownBy(
            () ->
                taskService.delete(
                    taskId,
                    new TaskDeleteParameters(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt)))
        .isInstanceOf(IllegalStateException.class);
    verify(taskRepository, never()).delete(any());
  }

  // ═════════════════════════════════════════════════════════════════════════
  // Edge cases — section 6
  // ═════════════════════════════════════════════════════════════════════════

  // ── 6.1 ──────────────────────────────────────────────────────────────────
  // Passing a occurrenceScheduledAt that does not match any real RRULE occurrence throws
  // ResourceNotFoundException, preventing orphan TaskOccurrenceState rows.

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

    assertThatThrownBy(() -> taskService.close(taskId, new TaskCloseReopenParameters(nonExistent)))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  // ── 6.6 (already tested as part of 4.6) ──────────────────────────────────

  // ── 6.7 ──────────────────────────────────────────────────────────────────
  // If the DB unique constraint fires (two concurrent requests for the same
  // occurrence), the DataIntegrityViolationException propagates unhandled.

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

    assertThatThrownBy(
            () -> taskService.close(taskId, new TaskCloseReopenParameters(occurrenceScheduledAt)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // ── Task not found ────────────────────────────────────────────────────────

  @Test
  void close_taskNotFound_throwsResourceNotFoundException() {
    UUID taskId = randomId();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.close(taskId, null));
  }

  @Test
  void delete_taskNotFound_throwsResourceNotFoundException() {
    UUID taskId = randomId();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.delete(taskId, null));
  }

  @Test
  void create_missingTypeDefaultsToTodo_andExplicitAppointmentIsPreserved() {
    Project inbox = new Project();
    inbox.setId(randomId());
    when(projectRepository.findByIsInboxProjectTrue()).thenReturn(Optional.of(inbox));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task todo = taskService.create(createParameters("Legacy-compatible task", TaskType.TODO));
    Task appointment = taskService.create(createParameters("Planning", TaskType.APPOINTMENT));

    assertThat(todo.getType()).isEqualTo(TaskType.TODO);
    assertThat(appointment.getType()).isEqualTo(TaskType.APPOINTMENT);
  }

  @Test
  void update_changesTaskTypeWithoutChangingContent() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    task.setType(TaskType.TODO);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.update(
        taskId,
        new TaskPatchParameters(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            TaskType.APPOINTMENT),
        false);

    assertThat(task.getType()).isEqualTo(TaskType.APPOINTMENT);
    assertThat(task.getContent()).isEqualTo("Non-recurring task");
  }
}
