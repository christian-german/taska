package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.taska.domain.task.controller.TaskDto;
import com.taska.domain.task.controller.TaskMapper;
import com.taska.domain.task.controller.TaskMapperImpl;
import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.occurrence.TaskInstanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.taska.domain.task.repository.Task;
import org.junit.jupiter.api.Test;

/**
 * Tests the default method TaskMapper#toOccurrenceDto, which merges a recurring Task with an
 * optional TaskInstance (field-level inheritance logic).
 */
class TaskMapperTest {

  private final TaskMapper taskMapper = new TaskMapperImpl();

  // ── Helpers ──────────────────────────────────────────────────────────────

  private Task buildTask(String content, Integer priority) {
    Task task = new Task();
    task.setId(UUID.randomUUID());
    task.setContent(content);
    task.setPriority(priority);
    task.setIsRecurring(true);
    task.setRecurrenceRule("FREQ=DAILY");
    task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    task.setLabels(List.of());
    return task;
  }

  private TaskInstance buildInstance(
      UUID taskId, Instant occurrenceScheduledAt, TaskInstanceStatus status) {
    TaskInstance taskInstance = new TaskInstance();
    taskInstance.setId(UUID.randomUUID());
    taskInstance.setTaskId(taskId);
    taskInstance.setOccurrenceScheduledAt(occurrenceScheduledAt);
    taskInstance.setStatus(status);
    return taskInstance;
  }

  // ── 2.2 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_noInstance_isVirtualTrueInstanceIdNull() {
    Task task = buildTask("My task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

    assertThat(taskDto.isVirtual()).isTrue();
    assertThat(taskDto.instanceId()).isNull();
    assertThat(taskDto.isCompleted()).isFalse();
    assertThat(taskDto.completedAt()).isNull();
  }

  // ── 2.3 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_doneInstance_isCompletedTrueIsVirtualFalse() {
    Task task = buildTask("My task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.DONE);
    instance.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

    assertThat(taskDto.isCompleted()).isTrue();
    assertThat(taskDto.isVirtual()).isFalse();
    assertThat(taskDto.completedAt()).isEqualTo(instance.getCompletedAt());
    assertThat(taskDto.instanceId()).isEqualTo(instance.getId());
  }

  // ── 2.5 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceWithTitleAndPriority_usesInstanceValues() {
    Task task = buildTask("Original title", 4);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
    instance.setTitle("Modified title");
    instance.setPriority(1);

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

    assertThat(taskDto.content()).isEqualTo("Modified title");
    assertThat(taskDto.priority()).isEqualTo(1);
  }

  // ── 2.6 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceNullTitle_inheritsParentContent() {
    Task task = buildTask("Parent content", 3);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
    instance.setTitle(null);
    instance.setPriority(2);

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

    assertThat(taskDto.content()).isEqualTo("Parent content");
    assertThat(taskDto.priority()).isEqualTo(2);
  }

  // ── 2.7 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceWithScheduledAt_usesInstanceScheduledAt() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant movedScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
    instance.setScheduledAt(movedScheduledAt);

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

    assertThat(taskDto.scheduledAt()).isEqualTo(movedScheduledAt);
  }

  @Test
  void toOccurrenceDto_dueAtInheritsFromTaskAndCanBeOverriddenByInstance() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant parentDueAt = Instant.parse("2026-05-21T17:00:00Z");
    Instant overrideDueAt = Instant.parse("2026-05-20T17:00:00Z");
    task.setDueAt(parentDueAt);

    assertThat(taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt).dueAt())
        .isEqualTo(parentDueAt);

    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
    instance.setDueAt(overrideDueAt);
    assertThat(taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt).dueAt())
        .isEqualTo(overrideDueAt);
  }

  // ── 6.5 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceAllOverrideFieldsNull_allInheritedFromParent() {
    Task task = buildTask("Parent content", 3);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskInstance instance =
        buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
    // title, priority and scheduledAt are all null — every field falls back to the parent

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

    assertThat(taskDto.content()).isEqualTo("Parent content");
    assertThat(taskDto.priority()).isEqualTo(3);
    assertThat(taskDto.scheduledAt())
        .isEqualTo(
            occurrenceScheduledAt); // falls back to occurrenceScheduledAt when no scheduledAt
    // override
  }

  // ── isRecurring / occurrenceScheduledAt always set ─────────────────────────────────

  @Test
  void toOccurrenceDto_alwaysSetsIsRecurringTrue() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

    assertThat(taskDto.isRecurring()).isTrue();
  }

  @Test
  void toOccurrenceDto_occurrenceScheduledAtPropagatedToDto() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    TaskDto taskDto = taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

    assertThat(taskDto.occurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
  }

  @Test
  void toOccurrenceDto_missingLegacyType_defaultsToTodo() {
    Task task = buildTask("Legacy task", 4);
    task.setType(null);

    assertThat(taskMapper.toOccurrenceDto(task, null, Instant.parse("2026-05-20T10:00:00Z")).type())
        .isEqualTo(TaskType.TODO);
  }

  @Test
  void toOccurrenceDto_appointmentType_isPreserved() {
    Task task = buildTask("Doctor visit", 4);
    task.setType(TaskType.APPOINTMENT);

    assertThat(taskMapper.toOccurrenceDto(task, null, Instant.parse("2026-05-20T10:00:00Z")).type())
        .isEqualTo(TaskType.APPOINTMENT);
  }
}
