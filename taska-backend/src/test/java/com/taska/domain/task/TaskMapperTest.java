package com.taska.domain.task;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the default method TaskMapper#toOccurrenceDto, which merges a recurring
 * Task with an optional TaskInstance (field-level inheritance logic).
 */
class TaskMapperTest {

    // Use an anonymous implementation so the default method runs without Spring context.
    private final TaskMapper mapper = _ -> {
        throw new UnsupportedOperationException("not used in these tests");
    };

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Task buildTask(String content, Integer priority) {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setContent(content);
        t.setPriority(priority);
        t.setIsRecurring(true);
        t.setRecurrenceRule("FREQ=DAILY");
        t.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
        t.setLabels(List.of());
        return t;
    }

    private TaskInstance buildInstance(UUID taskId, Instant occurrenceScheduledAt, TaskInstanceStatus status) {
        TaskInstance i = new TaskInstance();
        i.setId(UUID.randomUUID());
        i.setTaskId(taskId);
        i.setOccurrenceScheduledAt(occurrenceScheduledAt);
        i.setStatus(status);
        return i;
    }

    // ── 2.2 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_noInstance_isVirtualTrueInstanceIdNull() {
        Task task = buildTask("My task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        TaskDto dto = mapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

        assertThat(dto.isVirtual()).isTrue();
        assertThat(dto.instanceId()).isNull();
        assertThat(dto.isCompleted()).isFalse();
        assertThat(dto.completedAt()).isNull();
    }

    // ── 2.3 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_doneInstance_isCompletedTrueIsVirtualFalse() {
        Task task = buildTask("My task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.DONE);
        instance.setCompletedAt(Instant.parse("2026-05-20T11:00:00Z"));

        TaskDto dto = mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

        assertThat(dto.isCompleted()).isTrue();
        assertThat(dto.isVirtual()).isFalse();
        assertThat(dto.completedAt()).isEqualTo(instance.getCompletedAt());
        assertThat(dto.instanceId()).isEqualTo(instance.getId());
    }

    // ── 2.5 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_modifiedInstanceWithTitleAndPriority_usesInstanceValues() {
        Task task = buildTask("Original title", 4);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        instance.setTitle("Modified title");
        instance.setPriority(1);

        TaskDto dto = mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

        assertThat(dto.content()).isEqualTo("Modified title");
        assertThat(dto.priority()).isEqualTo(1);
    }

    // ── 2.6 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_modifiedInstanceNullTitle_inheritsParentContent() {
        Task task = buildTask("Parent content", 3);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        instance.setTitle(null);
        instance.setPriority(2);

        TaskDto dto = mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

        assertThat(dto.content()).isEqualTo("Parent content");
        assertThat(dto.priority()).isEqualTo(2);
    }

    // ── 2.7 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_modifiedInstanceWithScheduledAt_usesInstanceScheduledAt() {
        Task task = buildTask("Task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        Instant movedScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        instance.setScheduledAt(movedScheduledAt);

        TaskDto dto = mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

        assertThat(dto.scheduledAt()).isEqualTo(movedScheduledAt);
    }

    @Test
    void toOccurrenceDto_dueAtInheritsFromTaskAndCanBeOverriddenByInstance() {
        Task task = buildTask("Task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        Instant parentDueAt = Instant.parse("2026-05-21T17:00:00Z");
        Instant overrideDueAt = Instant.parse("2026-05-20T17:00:00Z");
        task.setDueAt(parentDueAt);

        assertThat(mapper.toOccurrenceDto(task, null, occurrenceScheduledAt).dueAt()).isEqualTo(parentDueAt);

        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        instance.setDueAt(overrideDueAt);
        assertThat(mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt).dueAt()).isEqualTo(overrideDueAt);
    }

    // ── 6.5 ──────────────────────────────────────────────────────────────────

    @Test
    void toOccurrenceDto_modifiedInstanceAllOverrideFieldsNull_allInheritedFromParent() {
        Task task = buildTask("Parent content", 3);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance instance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        // title, priority and scheduledAt are all null — every field falls back to the parent

        TaskDto dto = mapper.toOccurrenceDto(task, instance, occurrenceScheduledAt);

        assertThat(dto.content()).isEqualTo("Parent content");
        assertThat(dto.priority()).isEqualTo(3);
        assertThat(dto.scheduledAt()).isEqualTo(occurrenceScheduledAt); // falls back to occurrenceScheduledAt when no scheduledAt override
    }

    // ── isRecurring / occurrenceScheduledAt always set ─────────────────────────────────

    @Test
    void toOccurrenceDto_alwaysSetsIsRecurringTrue() {
        Task task = buildTask("Task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        TaskDto dto = mapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

        assertThat(dto.isRecurring()).isTrue();
    }

    @Test
    void toOccurrenceDto_occurrenceScheduledAtPropagatedToDto() {
        Task task = buildTask("Task", 2);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        TaskDto dto = mapper.toOccurrenceDto(task, null, occurrenceScheduledAt);

        assertThat(dto.occurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
    }

    @Test
    void toOccurrenceDto_missingLegacyType_defaultsToTodo() {
        Task task = buildTask("Legacy task", 4);
        task.setType(null);

        assertThat(mapper.toOccurrenceDto(task, null, Instant.parse("2026-05-20T10:00:00Z")).type())
                .isEqualTo(TaskType.TODO);
    }

    @Test
    void toOccurrenceDto_appointmentType_isPreserved() {
        Task task = buildTask("Doctor visit", 4);
        task.setType(TaskType.APPOINTMENT);

        assertThat(mapper.toOccurrenceDto(task, null, Instant.parse("2026-05-20T10:00:00Z")).type())
                .isEqualTo(TaskType.APPOINTMENT);
    }
}
