package com.taska.domain.task;

import com.taska.domain.priority.TaskPriorityEvaluationRepository;
import com.taska.domain.project.Project;
import com.taska.domain.project.ProjectRepository;
import com.taska.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService write operations: close, reopen, update, delete.
 * All repository calls are mocked. Tests focus on what gets persisted and
 * whether the parent task is left untouched when only an instance should change.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceMutationTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskInstanceRepository taskInstanceRepository;
    @Mock
    private RecurrenceService recurrenceService;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskPriorityEvaluationRepository priorityEvaluationRepository;

    @InjectMocks
    private TaskService service;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UUID randomId() {
        return UUID.randomUUID();
    }

    private Task buildNonRecurringTask(UUID id) {
        Task t = new Task();
        t.setId(id);
        t.setContent("Non-recurring task");
        t.setIsRecurring(false);
        t.setIsCompleted(false);
        t.setLabels(List.of());
        t.setPriority(4);
        return t;
    }

    private Task buildRecurringTask(UUID id) {
        Task t = new Task();
        t.setId(id);
        t.setContent("Recurring task");
        t.setIsRecurring(true);
        t.setRecurrenceRule("FREQ=DAILY");
        t.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
        t.setIsCompleted(false);
        t.setLabels(List.of());
        t.setPriority(4);
        return t;
    }

    private TaskInstance buildInstance(UUID taskId, Instant occurrenceScheduledAt, TaskInstanceStatus status) {
        TaskInstance i = new TaskInstance();
        i.setId(randomId());
        i.setTaskId(taskId);
        i.setOccurrenceScheduledAt(occurrenceScheduledAt);
        i.setStatus(status);
        return i;
    }

    private TaskDto anyDto() {
        return new TaskDto(randomId(), "t", null, null, null, null,
                0, 1, List.of(), false, null, false, false,
                null, null, null, null, null, null, null, null, false, null);
    }

    /**
     * Minimal TaskRequest with only the fields required for the test.
     * All unused fields are null.
     */
    private TaskRequest req(String content, RecurrenceScope scope, Instant occurrenceScheduledAt) {
        return new TaskRequest(content, null, null, null, null, null,
                null, null, null, null, null, null, null, null, scope, occurrenceScheduledAt);
    }

    @SuppressWarnings("SameParameterValue")
    private TaskRequest req(String content, Integer priority, RecurrenceScope scope, Instant occurrenceScheduledAt) {
        return new TaskRequest(content, null, null, null, null, null,
                priority, null, null, null, null, null, null, null, scope, occurrenceScheduledAt);
    }

    @SuppressWarnings("SameParameterValue")
    private TaskRequest reqWithScheduledAt(Instant scheduledAt, RecurrenceScope scope, Instant occurrenceScheduledAt) {
        return new TaskRequest(null, null, null, null, null, null,
                null, null, scheduledAt, null, null, null, null, null, scope, occurrenceScheduledAt);
    }

    @SuppressWarnings("SameParameterValue")
    private TaskRequest reqWithRRule(String content, String rrule, RecurrenceScope scope, Instant occurrenceScheduledAt) {
        return new TaskRequest(content, null, null, null, null, null,
                null, null, null, null, null, null, null, rrule, scope, occurrenceScheduledAt);
    }

    @Test
    void create_withoutManualPriority_persistsNullRatherThanNormalPriority() {
        UUID inboxId = randomId();
        Project inbox = new Project();
        inbox.setId(inboxId);
        when(projectRepository.findByIsInboxProjectTrue()).thenReturn(Optional.of(inbox));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = service.create(req("Unprioritized", null, null));

        assertThat(created.getPriority()).isNull();
        assertThat(created.getProjectId()).isEqualTo(inboxId);
    }

    @Test
    void update_withExplicitNullPriority_clearsManualPriority() {
        UUID taskId = randomId();
        Task task = buildNonRecurringTask(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(anyDto());

        service.update(taskId, req(null, null, null), true);

        assertThat(task.getPriority()).isNull();
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
        when(taskMapper.toDto(task)).thenReturn(anyDto());

        service.close(taskId, null);

        assertThat(task.getIsCompleted()).isTrue();
        assertThat(task.getCompletedAt()).isNotNull();
        verify(taskRepository).save(task);
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 3.2 ──────────────────────────────────────────────────────────────────

    @Test
    void close_recurringOccurrenceVirtual_createsTaskInstanceWithStatusDone() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        ArgumentCaptor<TaskInstance> captor = ArgumentCaptor.forClass(TaskInstance.class);
        when(taskInstanceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt));

        TaskInstance saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskInstanceStatus.DONE);
        assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
        assertThat(saved.getTaskId()).isEqualTo(taskId);
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
        TaskInstance existingDone = buildInstance(taskId, occurrenceScheduledAt, TaskInstanceStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt))
                .thenReturn(Optional.of(existingDone));

        assertThatThrownBy(() -> service.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 3.4 ──────────────────────────────────────────────────────────────────

    @Test
    void close_recurringOccurrence_parentTaskNotSaved() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt));

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

        assertThatThrownBy(() -> service.close(taskId, new TaskCloseReopenRequest(null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(taskInstanceRepository, never()).save(any());
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
        when(taskMapper.toDto(task)).thenReturn(anyDto());

        service.reopen(taskId, null);

        assertThat(task.getIsCompleted()).isFalse();
        assertThat(task.getCompletedAt()).isNull();
        verify(taskRepository).save(task);
    }

    // ── 3.7 ──────────────────────────────────────────────────────────────────

    @Test
    void reopen_recurringOccurrence_deletesTaskInstanceAndReturnsVirtualOccurrence() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt)).thenReturn(anyDto());

        service.reopen(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt));

        verify(taskInstanceRepository).deleteByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt);
        verify(taskMapper).toOccurrenceDto(task, null, occurrenceScheduledAt);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // update() — section 4
    // ═════════════════════════════════════════════════════════════════════════

    // ── 4.1 ──────────────────────────────────────────────────────────────────

    @Test
    void update_nonRecurringTask_patchesTaskDirectlyNoInstanceCreated() {
        UUID taskId = randomId();
        Task task = buildNonRecurringTask(taskId);
        TaskRequest request = req("Updated content", null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(anyDto());

        service.update(taskId, request);

        assertThat(task.getContent()).isEqualTo("Updated content");
        verify(taskRepository).save(task);
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 4.2 ──────────────────────────────────────────────────────────────────

    @Test
    void update_thisOnly_contentChange_createsModifiedInstanceWithTitle() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskRequest request = req("New content", RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        ArgumentCaptor<TaskInstance> captor = ArgumentCaptor.forClass(TaskInstance.class);
        when(taskInstanceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.update(taskId, request);

        TaskInstance saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskInstanceStatus.MODIFIED);
        assertThat(saved.getTitle()).isEqualTo("New content");
        assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
        assertThat(saved.getTaskId()).isEqualTo(taskId);
    }

    // ── 4.3 ──────────────────────────────────────────────────────────────────

    @Test
    void update_thisOnly_scheduledAtChange_instanceHasNewScheduledAtAndOriginalOccurrenceScheduledAt() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        Instant newScheduledAt = Instant.parse("2026-05-20T14:00:00Z");
        TaskRequest request = reqWithScheduledAt(newScheduledAt, RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        ArgumentCaptor<TaskInstance> captor = ArgumentCaptor.forClass(TaskInstance.class);
        when(taskInstanceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.update(taskId, request);

        TaskInstance saved = captor.getValue();
        assertThat(saved.getScheduledAt()).isEqualTo(newScheduledAt);
        assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt); // original theoretical time preserved
    }

    // ── 4.4 ──────────────────────────────────────────────────────────────────

    @Test
    void update_thisOnly_priorityChange_instanceHasNewPriority() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskRequest request = req("Recurring task", 1, RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        ArgumentCaptor<TaskInstance> captor = ArgumentCaptor.forClass(TaskInstance.class);
        when(taskInstanceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.update(taskId, request);

        TaskInstance saved = captor.getValue();
        assertThat(saved.getPriority()).isEqualTo(1);
    }

    // ── 4.5 ──────────────────────────────────────────────────────────────────

    @Test
    void update_thisOnly_parentTaskNotSaved() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskRequest request = req("New content", RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toOccurrenceDto(any(), any(), any())).thenReturn(anyDto());

        service.update(taskId, request);

        assertThat(task.getContent()).isEqualTo("Recurring task"); // content unchanged
        verify(taskRepository, never()).save(any());
    }

    // ── 4.6 ──────────────────────────────────────────────────────────────────

    @Test
    void update_fromThis_contentChange_truncatesOriginalAndClonesTaskWithNewContent() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskRequest request = req("New content", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(anyDto());

        service.update(taskId, request);

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
        TaskRequest request = req("Updated", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(anyDto());

        service.update(taskId, request);

        Task clone = taskCaptor.getAllValues().get(1);
        assertThat(clone.getProjectId()).isEqualTo(projectId);
        assertThat(clone.getParentId()).isEqualTo(parentId);
    }

    // ── 4.8 ──────────────────────────────────────────────────────────────────

    @Test
    void update_fromThis_firstOccurrence_originalRruleEndsAtIsOneDayBeforeScheduledAt() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant firstOccurrence = task.getScheduledAt(); // occurrenceScheduledAt == scheduledAt for the first occurrence
        TaskRequest request = req("Updated", RecurrenceScope.FROM_THIS, firstOccurrence);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(anyDto());

        service.update(taskId, request);

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
        TaskRequest request = req("Modified", RecurrenceScope.THIS_ONLY, null);

        assertThatThrownBy(() -> service.update(taskId, request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 4.10 ─────────────────────────────────────────────────────────────────

    @Test
    void update_fromThis_newRecurrenceRule_cloneHasNewRuleOriginalKeepsOldRule() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        String originalRule = task.getRecurrenceRule(); // FREQ=DAILY
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskRequest request = reqWithRRule("Updated", "FREQ=WEEKLY", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(anyDto());

        service.update(taskId, request);

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
        TaskRequest request = req("Updated", RecurrenceScope.FROM_THIS, occurrenceScheduledAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(anyDto());

        service.update(taskId, request);

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

        service.delete(taskId, null);

        verify(taskRepository).delete(task);
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 5.2 ──────────────────────────────────────────────────────────────────

    @Test
    void delete_recurringTaskNullBody_physicallyDeletesTask() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        service.delete(taskId, null);

        verify(taskRepository).delete(task);
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 5.3 ──────────────────────────────────────────────────────────────────

    @Test
    void delete_thisOnly_createsSkippedInstanceAndDoesNotDeleteTask() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        ArgumentCaptor<TaskInstance> captor = ArgumentCaptor.forClass(TaskInstance.class);
        when(taskInstanceRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.delete(taskId, new TaskDeleteRequest(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt));

        TaskInstance saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskInstanceStatus.SKIPPED);
        assertThat(saved.getOccurrenceScheduledAt()).isEqualTo(occurrenceScheduledAt);
        assertThat(saved.getTaskId()).isEqualTo(taskId);
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
        TaskInstance existing = buildInstance(taskId, occurrenceScheduledAt, TaskInstanceStatus.SKIPPED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.delete(taskId, new TaskDeleteRequest(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt)))
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

        service.delete(taskId, new TaskDeleteRequest(RecurrenceScope.FROM_THIS, occurrenceScheduledAt));

        assertThat(task.getRruleEndsAt()).isEqualTo(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
        verify(taskRepository).save(task);
        verify(taskRepository, never()).delete(any());
        verify(taskInstanceRepository, never()).save(any());
    }

    // ── 5.6 ──────────────────────────────────────────────────────────────────

    @Test
    void delete_fromThis_firstOccurrence_rruleEndsAtIsOneDayBeforeScheduledAt() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant firstOccurrence = task.getScheduledAt();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        service.delete(taskId, new TaskDeleteRequest(RecurrenceScope.FROM_THIS, firstOccurrence));

        assertThat(task.getRruleEndsAt()).isEqualTo(firstOccurrence.minus(1, ChronoUnit.SECONDS));
    }

    // ── 5.7 ──────────────────────────────────────────────────────────────────
    // Cascade deletion of task_instances is enforced at the DB level (FK CASCADE).
    // At the service level we verify that taskRepository.delete is called.

    @Test
    void delete_parentTask_callsRepositoryDeleteAndNoDirectInstanceDeletion() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        service.delete(taskId, null);

        verify(taskRepository).delete(task);
        verify(taskInstanceRepository, never()).deleteByTaskIdAndOccurrenceScheduledAt(any(), any());
    }

    // ── 5.8 ──────────────────────────────────────────────────────────────────
    // THIS_ONLY on a DONE occurrence throws IllegalStateException.
    // The occurrence must be reopened before it can be skipped.

    @Test
    void delete_thisOnly_occurrencePreviouslyDone_throwsIllegalStateException() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance existingDone = buildInstance(taskId, occurrenceScheduledAt, TaskInstanceStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt))
                .thenReturn(Optional.of(existingDone));

        assertThatThrownBy(() ->
                service.delete(taskId, new TaskDeleteRequest(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt)))
                .isInstanceOf(IllegalStateException.class);
        verify(taskRepository, never()).delete(any());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Edge cases — section 6
    // ═════════════════════════════════════════════════════════════════════════

    // ── 6.1 ──────────────────────────────────────────────────────────────────
    // Passing a occurrenceScheduledAt that does not match any real RRULE occurrence throws
    // ResourceNotFoundException, preventing orphan TaskInstance rows.

    @Test
    void close_occurrenceScheduledAtNotMatchingAnyRealOccurrence_throwsResourceNotFoundException() {
        UUID taskId = randomId();
        Task task = buildRecurringTask(taskId);
        // This timestamp is not a valid FREQ=DAILY occurrence at 10:00
        Instant nonExistent = Instant.parse("2026-05-20T13:37:00Z");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        // getOccurrencesInRange returns the real occurrences (10:00), not the bogus one
        when(recurrenceService.getOccurrencesInRange(any(), any(), any()))
                .thenReturn(List.of(Instant.parse("2026-05-20T10:00:00Z")));

        assertThatThrownBy(() -> service.close(taskId, new TaskCloseReopenRequest(nonExistent)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskInstanceRepository, never()).save(any());
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
        when(recurrenceService.getOccurrencesInRange(any(), any(), any())).thenReturn(List.of(occurrenceScheduledAt));
        when(taskInstanceRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> service.close(taskId, new TaskCloseReopenRequest(occurrenceScheduledAt)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── Task not found ────────────────────────────────────────────────────────

    @Test
    void close_taskNotFound_throwsResourceNotFoundException() {
        UUID taskId = randomId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.close(taskId, null));
    }

    @Test
    void delete_taskNotFound_throwsResourceNotFoundException() {
        UUID taskId = randomId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.delete(taskId, null));
    }

    @Test
    void create_missingTypeDefaultsToTodo_andExplicitAppointmentIsPreserved() {
        Project inbox = new Project();
        inbox.setId(randomId());
        when(projectRepository.findByIsInboxProjectTrue()).thenReturn(Optional.of(inbox));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task todo = service.create(req("Legacy-compatible task", null, null));
        Task appointment = service.create(new TaskRequest("Planning", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, TaskType.APPOINTMENT));

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
        when(taskMapper.toDto(task)).thenReturn(anyDto());

        service.update(taskId, new TaskRequest(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, TaskType.APPOINTMENT));

        assertThat(task.getType()).isEqualTo(TaskType.APPOINTMENT);
        assertThat(task.getContent()).isEqualTo("Non-recurring task");
    }
}
