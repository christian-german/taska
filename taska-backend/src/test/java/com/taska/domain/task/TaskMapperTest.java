package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.taska.domain.task.controller.NonRecurringTaskDto;
import com.taska.domain.task.controller.RecurringTaskOccurrenceDto;
import com.taska.domain.task.controller.RecurringTaskSeriesDto;
import com.taska.domain.task.controller.TaskMapper;
import com.taska.domain.task.controller.TaskMapperImpl;
import com.taska.domain.task.controller.TaskRepresentationKind;
import com.taska.domain.task.occurrence.TaskOccurrenceState;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Tests the default method TaskMapper#toOccurrenceDto, which merges a recurring Task with an
 * optional TaskOccurrenceState (field-level inheritance logic).
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

  private TaskOccurrenceState buildOccurrenceState(
      UUID seriesId, Instant occurrenceScheduledAt, TaskOccurrenceStatus status) {
    TaskOccurrenceState occurrenceState = new TaskOccurrenceState();
    occurrenceState.setId(UUID.randomUUID());
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(status);
    return occurrenceState;
  }

  @Test
  void toDto_nonRecurringTask_returnsNonRecurringRepresentation() {
    Task task = buildTask("One task", 2);
    task.setIsRecurring(false);
    task.setRecurrenceRule(null);

    assertThat(taskMapper.toDto(task))
        .isInstanceOf(NonRecurringTaskDto.class)
        .extracting("kind")
        .isEqualTo(TaskRepresentationKind.NON_RECURRING);
  }

  @Test
  void toDto_recurringTask_returnsSeriesRepresentation() {
    Task task = buildTask("Daily task", 2);

    assertThat(taskMapper.toDto(task))
        .isInstanceOf(RecurringTaskSeriesDto.class)
        .extracting("kind")
        .isEqualTo(TaskRepresentationKind.RECURRING_SERIES);
  }

  // ── 2.2 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_noInstance_isVirtualTrueInstanceIdNull() {
    Task task = buildTask("My task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, null, occurrenceScheduledAt));

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
    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.DONE);
    instance.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt));

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
    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    instance.setTitle("Modified title");
    instance.setPriority(1);

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt));

    assertThat(taskDto.content()).isEqualTo("Modified title");
    assertThat(taskDto.priority()).isEqualTo(1);
  }

  // ── 2.6 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceNullTitle_inheritsParentContent() {
    Task task = buildTask("Parent content", 3);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    instance.setTitle(null);
    instance.setPriority(2);

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt));

    assertThat(taskDto.content()).isEqualTo("Parent content");
    assertThat(taskDto.priority()).isEqualTo(2);
  }

  // ── 2.7 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceWithScheduledAt_usesInstanceScheduledAt() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant movedScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    instance.setScheduledAt(movedScheduledAt);

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt));

    assertThat(taskDto.scheduledAt()).isEqualTo(movedScheduledAt);
  }

  @Test
  void toOccurrenceDto_dueAtInheritsFromTaskAndCanBeOverriddenByInstance() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Instant parentDueAt = Instant.parse("2026-05-21T17:00:00Z");
    Instant overrideDueAt = Instant.parse("2026-05-20T17:00:00Z");
    task.setDueAt(parentDueAt);

    assertThat(
            taskMapper
                .toOccurrenceDto(
                    new RecurringTaskOccurrenceResult(task, null, occurrenceScheduledAt))
                .dueAt())
        .isEqualTo(parentDueAt);

    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    instance.setDueAt(overrideDueAt);
    assertThat(
            taskMapper
                .toOccurrenceDto(
                    new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt))
                .dueAt())
        .isEqualTo(overrideDueAt);
  }

  // ── 6.5 ──────────────────────────────────────────────────────────────────

  @Test
  void toOccurrenceDto_modifiedInstanceAllOverrideFieldsNull_allInheritedFromParent() {
    Task task = buildTask("Parent content", 3);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskOccurrenceState instance =
        buildOccurrenceState(task.getId(), occurrenceScheduledAt, TaskOccurrenceStatus.MODIFIED);
    // title, priority and scheduledAt are all null — every field falls back to the parent

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, instance, occurrenceScheduledAt));

    assertThat(taskDto.content()).isEqualTo("Parent content");
    assertThat(taskDto.priority()).isEqualTo(3);
    assertThat(taskDto.scheduledAt())
        .isEqualTo(
            occurrenceScheduledAt); // falls back to occurrenceScheduledAt when no scheduledAt
    // override
  }

  // ── discriminator / occurrenceScheduledAt always set ───────────────────────────────

  @Test
  void toOccurrenceDto_alwaysSetsOccurrenceDiscriminator() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, null, occurrenceScheduledAt));

    assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
  }

  @Test
  void toOccurrenceDto_occurrenceScheduledAtPropagatedToDto() {
    Task task = buildTask("Task", 2);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    RecurringTaskOccurrenceDto taskDto =
        taskMapper.toOccurrenceDto(
            new RecurringTaskOccurrenceResult(task, null, occurrenceScheduledAt));

    assertThat(taskDto.occurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
  }

  @Test
  void toOccurrenceDto_missingLegacyType_defaultsToTodo() {
    Task task = buildTask("Legacy task", 4);
    task.setType(null);

    assertThat(
            taskMapper
                .toOccurrenceDto(
                    new RecurringTaskOccurrenceResult(
                        task, null, Instant.parse("2026-05-20T10:00:00Z")))
                .type())
        .isEqualTo(TaskType.TODO);
  }

  @Test
  void toOccurrenceDto_appointmentType_isPreserved() {
    Task task = buildTask("Doctor visit", 4);
    task.setType(TaskType.APPOINTMENT);

    assertThat(
            taskMapper
                .toOccurrenceDto(
                    new RecurringTaskOccurrenceResult(
                        task, null, Instant.parse("2026-05-20T10:00:00Z")))
                .type())
        .isEqualTo(TaskType.APPOINTMENT);
  }
}
