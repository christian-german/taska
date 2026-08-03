package com.taska.domain.task;

import com.taska.config.TaskaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService#findOccurrencesForDateRange.
 * All repository and RecurrenceService calls are mocked; TaskMapper is mocked
 * to isolate service routing logic from field-inheritance logic (see TaskMapperTest).
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceQueryTest {

    @Mock private TaskRepository          taskRepository;
    @Mock private TaskInstanceRepository  taskInstanceRepository;
    @Mock private RecurrenceService       recurrenceService;
    @Mock private TaskMapper              taskMapper;
    @Mock private TaskaProperties         taskaProperties;
    @Mock private TaskaProperties.Calendar calendarProperties;

    @InjectMocks
    private TaskService service;

    // ── Period used by most single-day tests ─────────────────────────────────

    private static final LocalDate DATE  = LocalDate.parse("2026-05-20");
    private static final Instant   START = DATE.atStartOfDay(ZoneOffset.UTC).toInstant();
    private static final Instant   END   = DATE.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    @BeforeEach
    void useUtcForExistingQueryScenarios() {
        when(taskaProperties.getCalendar()).thenReturn(calendarProperties);
        when(calendarProperties.getTimeZone()).thenReturn(ZoneOffset.UTC);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Task buildNonRecurringTask() {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setContent("Non-recurring");
        t.setIsRecurring(false);
        t.setLabels(List.of());
        return t;
    }

    private Task buildRecurringTask() {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setContent("Recurring");
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

    /** Minimal stub DTO — content does not matter for routing tests. */
    private TaskDto stubDto(UUID id) {
        return new TaskDto(id, "content", null, null, null, null,
                0, 1, List.of(), false, null, false, false,
                null, null, null, null, null, null, null, null, false, null);
    }

    @SuppressWarnings("SameParameterValue")
    private void givenNoRecurringTasks(Instant start, Instant end) {
        when(taskRepository.findActiveRecurringTasksForPeriod(start, end)).thenReturn(List.of());
    }

    private void useCalendarZone(ZoneId zone) {
        when(calendarProperties.getTimeZone()).thenReturn(zone);
    }

    @Test
    void findAll_todayUsesConfiguredCalendarZoneBoundaries() {
        ZoneId paris = ZoneId.of("Europe/Paris");
        useCalendarZone(paris);
        LocalDate today = LocalDate.now(paris);
        Instant start = today.atStartOfDay(paris).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(paris).toInstant();

        service.findAll(null, null, null, "today", false);

        verify(taskRepository).findByScheduledAtBetweenAndIsCompletedFalseOrderByScheduledAtAsc(start, end);
    }

    @Test
    void findOccurrencesForDateRange_usesConfiguredLocalDateBoundaries() {
        ZoneId paris = ZoneId.of("Europe/Paris");
        useCalendarZone(paris);
        Instant start = Instant.parse("2026-05-19T22:00:00Z");
        Instant end = Instant.parse("2026-05-20T22:00:00Z");

        when(taskRepository.findNonRecurringTasksInPeriod(start, end)).thenReturn(List.of());
        givenNoRecurringTasks(start, end);

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).isEmpty();
        verify(taskRepository).findNonRecurringTasksInPeriod(start, end);
    }

    @Test
    void findOccurrencesForDateRange_dstDayUsesSuccessiveLocalMidnights() {
        ZoneId paris = ZoneId.of("Europe/Paris");
        useCalendarZone(paris);
        LocalDate dstStart = LocalDate.parse("2026-03-29");
        Instant start = Instant.parse("2026-03-28T23:00:00Z");
        Instant end = Instant.parse("2026-03-29T22:00:00Z");

        when(taskRepository.findNonRecurringTasksInPeriod(start, end)).thenReturn(List.of());
        givenNoRecurringTasks(start, end);

        service.findOccurrencesForDateRange(dstStart, dstStart);

        verify(taskRepository).findNonRecurringTasksInPeriod(start, end);
        assertThat(end).isEqualTo(start.plusSeconds(23 * 60 * 60));
    }

    // ── 2.1 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_nonRecurringTaskInWindow_returnedViaToDtoNotOccurrenceDto() {
        Task task = buildNonRecurringTask();
        TaskDto dto = stubDto(task.getId());

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of(task));
        givenNoRecurringTasks(START, END);
        when(taskMapper.toDto(task)).thenReturn(dto);

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).containsExactly(dto);
        verify(taskMapper).toDto(task);
        verify(taskMapper, never()).toOccurrenceDto(any(), any(), any());
    }

    // ── 2.2 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_recurringOccurrenceNoInstance_mapperCalledWithNullInstance() {
        Task task = buildRecurringTask();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskDto occDto = stubDto(task.getId());

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(START, END)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(eq(List.of(task.getId())), eq(START), eq(END)))
                .thenReturn(List.of());
        when(recurrenceService.getOccurrencesInRange(task, START, END)).thenReturn(List.of(occurrenceScheduledAt));
        when(taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt)).thenReturn(occDto);

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).containsExactly(occDto);
        // null instance → virtual occurrence
        verify(taskMapper).toOccurrenceDto(task, null, occurrenceScheduledAt);
    }

    // ── 2.3 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_doneInstance_mapperCalledWithDoneInstance() {
        Task task = buildRecurringTask();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance doneInstance = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.DONE);
        TaskDto occDto = stubDto(task.getId());

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(START, END)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(eq(List.of(task.getId())), eq(START), eq(END)))
                .thenReturn(List.of(doneInstance));
        when(recurrenceService.getOccurrencesInRange(task, START, END)).thenReturn(List.of(occurrenceScheduledAt));
        when(taskMapper.toOccurrenceDto(task, doneInstance, occurrenceScheduledAt)).thenReturn(occDto);

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).containsExactly(occDto);
        verify(taskMapper).toOccurrenceDto(task, doneInstance, occurrenceScheduledAt);
    }

    // ── 2.4 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_skippedInstance_occurrenceHiddenFromResult() {
        Task task = buildRecurringTask();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskInstance skipped = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.SKIPPED);

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(START, END)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(eq(List.of(task.getId())), eq(START), eq(END)))
                .thenReturn(List.of(skipped));
        when(recurrenceService.getOccurrencesInRange(task, START, END)).thenReturn(List.of(occurrenceScheduledAt));

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).isEmpty();
        verify(taskMapper, never()).toOccurrenceDto(any(), any(), any());
    }

    // ── 2.8 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_recurrenceServiceReturnsEmpty_noOccurrences() {
        Task task = buildRecurringTask();
        task.setRruleEndsAt(Instant.parse("2026-05-19T23:59:59Z")); // ended before this window

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(START, END)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(anyList(), any(), any()))
                .thenReturn(List.of());
        when(recurrenceService.getOccurrencesInRange(task, START, END)).thenReturn(List.of());

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).isEmpty();
    }

    // ── 2.9 ──────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_twoRecurringTasksSameDay_bothOccurrencesReturned() {
        Task task1 = buildRecurringTask();
        Task task2 = buildRecurringTask();
        Instant scheduled1 = Instant.parse("2026-05-20T08:00:00Z");
        Instant scheduled2 = Instant.parse("2026-05-20T10:00:00Z");
        TaskDto dto1 = stubDto(task1.getId());
        TaskDto dto2 = stubDto(task2.getId());

        when(taskRepository.findNonRecurringTasksInPeriod(START, END)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(START, END)).thenReturn(List.of(task1, task2));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(anyList(), eq(START), eq(END)))
                .thenReturn(List.of());
        when(recurrenceService.getOccurrencesInRange(task1, START, END)).thenReturn(List.of(scheduled1));
        when(recurrenceService.getOccurrencesInRange(task2, START, END)).thenReturn(List.of(scheduled2));
        when(taskMapper.toOccurrenceDto(task1, null, scheduled1)).thenReturn(dto1);
        when(taskMapper.toOccurrenceDto(task2, null, scheduled2)).thenReturn(dto2);

        List<TaskDto> result = service.findOccurrencesForDateRange(DATE, DATE);

        assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
    }

    // ── 2.11 ─────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_modifiedInstanceScheduledAtMovedToFutureDay_doesNotAppearOnOriginalDay() {
        // A daily task whose May-21 occurrence was rescheduled (THIS_ONLY) to May 23.
        // Querying May 21 should return nothing: the occurrence's actual scheduledAt is now May 23.
        LocalDate originalDay  = LocalDate.parse("2026-05-21");
        Instant   originalStart = originalDay.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant   originalEnd   = originalDay.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Task task = buildRecurringTask();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-21T10:00:00Z");
        Instant movedScheduledAt  = Instant.parse("2026-05-23T10:00:00Z");

        TaskInstance modified = buildInstance(task.getId(), occurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        modified.setScheduledAt(movedScheduledAt);

        when(taskRepository.findNonRecurringTasksInPeriod(originalStart, originalEnd)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(originalStart, originalEnd)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(
                eq(List.of(task.getId())), eq(originalStart), eq(originalEnd)))
                .thenReturn(List.of(modified));
        // movedScheduledAt (May 23) is outside the May-21 window → no moved-in instances.
        when(taskInstanceRepository.findByTaskIdInAndStatusAndScheduledAtBetween(
                eq(List.of(task.getId())), eq(TaskInstanceStatus.MODIFIED), eq(originalStart), eq(originalEnd)))
                .thenReturn(List.of());
        when(recurrenceService.getOccurrencesInRange(task, originalStart, originalEnd))
                .thenReturn(List.of(occurrenceScheduledAt));

        List<TaskDto> result = service.findOccurrencesForDateRange(originalDay, originalDay);

        // Occurrence moved to May 23 — May 21 should be empty.
        assertThat(result).isEmpty();
    }

    // ── 2.12 ─────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_modifiedInstanceScheduledAtMovedToFutureDay_appearsOnNewDay() {
        // Same rescheduled occurrence (occurrenceScheduledAt=May21, scheduledAt=May23).
        // Querying May 23 should make the service pass the MODIFIED instance to the mapper,
        // not null, so the occurrence is represented with its persisted instanceId.
        LocalDate newDay   = LocalDate.parse("2026-05-23");
        Instant   newStart = newDay.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant   newEnd   = newDay.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Task task = buildRecurringTask();
        Instant originalOccurrenceScheduledAt = Instant.parse("2026-05-21T10:00:00Z");
        Instant movedScheduledAt          = Instant.parse("2026-05-23T10:00:00Z");

        TaskInstance modified = buildInstance(task.getId(), originalOccurrenceScheduledAt, TaskInstanceStatus.MODIFIED);
        modified.setScheduledAt(movedScheduledAt);

        when(taskRepository.findNonRecurringTasksInPeriod(newStart, newEnd)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(newStart, newEnd)).thenReturn(List.of(task));
        // occurrenceScheduledAt=May21 is outside the May-23 window → not returned by occurrenceScheduledAt query.
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(
                eq(List.of(task.getId())), eq(newStart), eq(newEnd)))
                .thenReturn(List.of());
        // scheduledAt=May23 is inside the May-23 window → returned by scheduledAt query.
        when(taskInstanceRepository.findByTaskIdInAndStatusAndScheduledAtBetween(
                eq(List.of(task.getId())), eq(TaskInstanceStatus.MODIFIED), eq(newStart), eq(newEnd)))
                .thenReturn(List.of(modified));
        when(recurrenceService.getOccurrencesInRange(task, newStart, newEnd))
                .thenReturn(List.of(movedScheduledAt));

        service.findOccurrencesForDateRange(newDay, newDay);

        // The mapper must receive the MODIFIED instance, not null.
        verify(taskMapper).toOccurrenceDto(eq(task), eq(modified), any());
    }

    // ── 2.10 ─────────────────────────────────────────────────────────────────

    @Test
    void findOccurrencesForDateRange_mixedInstances_skippedHiddenDoneAndVirtualShown() {
        LocalDate from  = LocalDate.parse("2026-05-20");
        LocalDate to    = LocalDate.parse("2026-05-22");
        Instant   start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant   end   = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Task task = buildRecurringTask();
        Instant atSkipped = Instant.parse("2026-05-20T10:00:00Z");
        Instant atDone    = Instant.parse("2026-05-21T10:00:00Z");
        Instant atVirtual = Instant.parse("2026-05-22T10:00:00Z");

        TaskInstance skipped = buildInstance(task.getId(), atSkipped, TaskInstanceStatus.SKIPPED);
        TaskInstance done    = buildInstance(task.getId(), atDone,    TaskInstanceStatus.DONE);
        TaskDto doneDto    = stubDto(task.getId());
        TaskDto virtualDto = stubDto(task.getId());

        when(taskRepository.findNonRecurringTasksInPeriod(start, end)).thenReturn(List.of());
        when(taskRepository.findActiveRecurringTasksForPeriod(start, end)).thenReturn(List.of(task));
        when(taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(eq(List.of(task.getId())), eq(start), eq(end)))
                .thenReturn(List.of(skipped, done));
        when(recurrenceService.getOccurrencesInRange(task, start, end))
                .thenReturn(List.of(atSkipped, atDone, atVirtual));
        when(taskMapper.toOccurrenceDto(task, done,    atDone)).thenReturn(doneDto);
        when(taskMapper.toOccurrenceDto(task, null,    atVirtual)).thenReturn(virtualDto);

        List<TaskDto> result = service.findOccurrencesForDateRange(from, to);

        assertThat(result).containsExactlyInAnyOrder(doneDto, virtualDto);
        verify(taskMapper, never()).toOccurrenceDto(eq(task), eq(skipped), any());
    }
}
