package com.taska.domain.task;

import static com.taska.domain.task.TaskMutationFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.Project;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskService;
import com.taska.domain.task.service.TaskUpdateParameters;
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

/**
 * Write operations on a stored task definition: creation, patching, complete replacement,
 * completion and deletion. Recurring behaviour appears here only where a task definition must
 * refuse it.
 *
 * <p>All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceMutationTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private TaskPriorityEvaluationRepository priorityEvaluationRepository;
  @Mock private PlanningCalendarService planningCalendarService;

  private TaskService taskService;

  @BeforeEach
  void createServicesUnderTest() {
    taskService =
        new TaskService(
            taskRepository,
            projectRepository,
            priorityEvaluationRepository,
            planningCalendarService);
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
  void create_recurringSeriesWithDueAt_rejectsDeadline() {
    Instant dueAt = Instant.parse("2026-05-21T17:00:00Z");
    TaskCreateParameters parameters =
        new TaskCreateParameters(
            "Recurring",
            null,
            randomId(),
            null,
            0,
            null,
            List.of(),
            Instant.parse("2026-05-20T09:00:00Z"),
            dueAt,
            false,
            true,
            null,
            null,
            "FREQ=DAILY",
            TaskType.TODO);

    assertThatThrownBy(() -> taskService.create(parameters))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recurring series");
    verify(taskRepository, never()).save(any());
  }

  @Test
  void update_withExplicitNullPriority_clearsManualPriority() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.updateTask(taskId, taskRequest(null, null, null), true);

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

    taskService.updateTask(
        taskId,
        new TaskPatchParameters(
            null, null, null, null, null, null, null, null, dueAt, null, null, null, null, null,
            null, null, null),
        false);

    assertThat(task.getDueAt()).isEqualTo(dueAt);
    assertThat(task.getScheduledAt()).isEqualTo(scheduledAt);
  }

  @Test
  void update_convertingTaskToRecurring_clearsExistingDeadline() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    task.setDueAt(Instant.parse("2026-05-21T17:00:00Z"));
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.updateTask(taskId, recurringPatch(null), false);

    assertThat(task.getIsRecurring()).isTrue();
    assertThat(task.getDueAt()).isNull();
  }

  @Test
  void update_recurringSeriesWithDueAt_rejectsDeadline() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    assertThatThrownBy(
            () ->
                taskService.updateTask(
                    taskId, recurringPatch(Instant.parse("2026-05-21T17:00:00Z")), false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recurring series");
    verify(taskRepository, never()).save(any());
  }

  @Test
  void replace_recurringSeriesWithDueAt_rejectsDeadlineWithoutMutation() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    TaskUpdateParameters request =
        new TaskUpdateParameters(
            "Recurring",
            TaskType.TODO,
            null,
            null,
            null,
            0,
            null,
            List.of(),
            Instant.parse("2026-05-20T09:00:00Z"),
            Instant.parse("2026-05-21T17:00:00Z"),
            false,
            true,
            null,
            null,
            "FREQ=DAILY");

    assertThatThrownBy(() -> taskService.replace(taskId, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recurring series");

    assertThat(task.getContent()).isEqualTo("Non-recurring task");
    assertThat(task.getIsRecurring()).isFalse();
    verify(taskRepository, never()).save(any());
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
  void close_nonRecurringTask_setsIsCompletedTrueAndCompletedAtOnTask() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.closeTask(taskId);

    assertThat(task.getIsCompleted()).isTrue();
    assertThat(task.getCompletedAt()).isNotNull();
    verify(taskRepository).save(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void reopen_nonRecurringTask_setsIsCompletedFalseAndClearsCompletedAt() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    task.setIsCompleted(true);
    task.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.reopenTask(taskId);

    assertThat(task.getIsCompleted()).isFalse();
    assertThat(task.getCompletedAt()).isNull();
    verify(taskRepository).save(task);
  }

  @Test
  void reopen_recurringTaskWithoutOccurrence_throwsWithoutSavingSeries() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> taskService.reopenTask(taskId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("occurrenceScheduledAt");

    verify(taskRepository, never()).save(any());
    assertThat(task.getIsCompleted()).isFalse();
  }

  @Test
  void update_nonRecurringTask_patchesTaskDirectlyNoOccurrenceStateCreated() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    TaskPatchParameters request = taskRequest("Updated content", null, null);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    taskService.updateTask(taskId, request, request.priority() != null);

    assertThat(task.getContent()).isEqualTo("Updated content");
    verify(taskRepository).save(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void delete_nonRecurringTaskNullBody_physicallyDeletesTask() {
    UUID taskId = randomId();
    Task task = buildNonRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.deleteTask(taskId);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void delete_recurringTaskNullBody_physicallyDeletesTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.deleteTask(taskId);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void delete_parentTask_callsRepositoryDeleteAndNoDirectOccurrenceStateDeletion() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.deleteTask(taskId);

    verify(taskRepository).delete(task);
    verify(taskOccurrenceStateRepository, never())
        .deleteBySeriesIdAndOccurrenceScheduledAt(any(), any());
  }

  @Test
  void close_taskNotFound_throwsResourceNotFoundException() {
    UUID taskId = randomId();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.closeTask(taskId));
  }

  @Test
  void delete_taskNotFound_throwsResourceNotFoundException() {
    UUID taskId = randomId();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(taskId));
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

    taskService.updateTask(
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
