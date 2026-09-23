package com.taska.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.taska.task.adapter.http.TaskCloseReopenRequest;
import com.taska.task.adapter.http.TaskController;
import com.taska.task.adapter.http.TaskDto;
import com.taska.task.adapter.http.TaskMapperImpl;
import com.taska.task.adapter.http.TaskRepresentationKind;
import com.taska.task.application.TaskMutationService;
import com.taska.task.application.definition.TaskDefinitionService;
import com.taska.task.application.occurrence.TaskOccurrenceService;
import com.taska.task.model.RecurringTaskOccurrenceResult;
import com.taska.task.model.Task;
import com.taska.task.model.TaskOccurrenceState;
import com.taska.task.model.TaskOccurrenceUpdateParameters;
import com.taska.task.model.TaskResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Locks the variant each endpoint is allowed to return. The representation contract states which kinds a caller may receive per route; without these
 * assertions nothing prevents a query change from leaking a series definition into a calendar listing, or an occurrence into an undated list.
 */
class TaskControllerRepresentationTest {

    private static final LocalDate DATE = LocalDate.parse("2026-05-20");
    private static final Instant OCCURRENCE = Instant.parse("2026-05-20T09:00:00Z");

    private final TaskDefinitionService taskService = mock(TaskDefinitionService.class);
    private final TaskOccurrenceService taskOccurrenceService = mock(TaskOccurrenceService.class);
    private final TaskMutationService taskMutationService = mock(TaskMutationService.class);
    private final TaskController taskController = new TaskController(taskService, taskOccurrenceService, taskMutationService, new TaskMapperImpl());

    @Test
    void dateRangeListingReturnsNonRecurringTasksAndOccurrencesButNeverASeriesDefinition() {
        when(taskOccurrenceService.findOccurrencesForDateRange(DATE, DATE, false))
                .thenReturn(List.of(TaskResult.base(nonRecurringTask()), occurrenceResult(null)));

        List<TaskDto> tasks = taskController.getAll(null, null, false, DATE, null, null);

        assertThat(tasks).extracting(TaskDto::kind)
                .containsExactly(TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_OCCURRENCE);
        assertThat(tasks).extracting(TaskDto::kind).doesNotContain(TaskRepresentationKind.RECURRING_SERIES);
    }

    @Test
    void overdueListingReturnsNonRecurringTasksAndOccurrencesButNeverASeriesDefinition() {
        when(taskOccurrenceService.findOverdueOccurrences()).thenReturn(List.of(TaskResult.base(nonRecurringTask()), occurrenceResult(null)));

        List<TaskDto> tasks = taskController.getOverdue();

        assertThat(tasks).extracting(TaskDto::kind)
                .containsExactly(TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_OCCURRENCE);
        assertThat(tasks).extracting(TaskDto::kind).doesNotContain(TaskRepresentationKind.RECURRING_SERIES);
    }

    @Test
    void openEndedDateRangeListingFollowsTheSameContractAsASingleDay() {
        when(taskOccurrenceService.findOccurrencesForDateRange(DATE, DATE.plusDays(6), false)).thenReturn(List.of(occurrenceResult(null)));

        List<TaskDto> tasks = taskController.getAll(null, null, false, null, DATE, DATE.plusDays(6));

        assertThat(tasks).singleElement().extracting(TaskDto::kind).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
    }

    @Test
    void undatedListingReturnsSeriesDefinitionsRatherThanExpandedOccurrences() {
        when(taskService.findAll(null, null, false)).thenReturn(List.of(nonRecurringTask(), recurringTask()));

        List<TaskDto> tasks = taskController.getAll(null, null, false, null, null, null);

        assertThat(tasks).extracting(TaskDto::kind).containsExactly(TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_SERIES);
    }

    @Test
    void singleTaskRetrievalReturnsTheSeriesDefinitionOfARecurringTask() {
        Task task = recurringTask();
        when(taskService.findById(task.getId())).thenReturn(task);

        assertThat(taskController.getById(task.getId()).kind()).isEqualTo(TaskRepresentationKind.RECURRING_SERIES);
    }

    @Test
    void singleOccurrenceRetrievalReturnsTheOccurrenceRepresentation() {
        Task task = recurringTask();
        TaskOccurrenceState detached = new TaskOccurrenceState();
        detached.setId(UUID.randomUUID());
        detached.setDetached(true);
        when(taskOccurrenceService.findOccurrence(task.getId(), OCCURRENCE))
                .thenReturn(new RecurringTaskOccurrenceResult(task, detached, OCCURRENCE));

        TaskDto taskDto = taskController.getOccurrence(task.getId(), OCCURRENCE);

        assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
        assertThat(((com.taska.task.adapter.http.RecurringTaskOccurrenceDto) taskDto).isDetached()).isTrue();
    }

    @Test
    void subtaskListingNeverReturnsOccurrences() {
        UUID parentId = UUID.randomUUID();
        when(taskService.getSubtasks(parentId)).thenReturn(List.of(nonRecurringTask(), recurringTask()));

        assertThat(taskController.getSubtasks(parentId)).extracting(TaskDto::kind)
                .containsExactly(TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_SERIES);
    }

    @Test
    void occurrenceReplacementReturnsAnOccurrenceRepresentation() {
        Task task = recurringTask();
        TaskOccurrenceState instance = new TaskOccurrenceState();
        instance.setId(UUID.randomUUID());
        when(
                taskMutationService.replaceOccurrence(
                        org.mockito.ArgumentMatchers.eq(task.getId()),
                        org.mockito.ArgumentMatchers.eq(OCCURRENCE),
                        org.mockito.ArgumentMatchers.any(TaskOccurrenceUpdateParameters.class),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(new RecurringTaskOccurrenceResult(task, instance, OCCURRENCE));

        TaskDto taskDto = taskController
                .replaceOccurrence(task.getId(), OCCURRENCE, new com.taska.task.adapter.http.OccurrenceUpdateRequest(null), jwt());

        assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
    }

    @Test
    void closeReturnsAnOccurrenceRepresentationForARecurringOccurrence() {
        Task task = recurringTask();
        when(taskMutationService.close(task.getId(), new com.taska.task.model.TaskCloseReopenParameters(OCCURRENCE), "account-a"))
                .thenReturn(occurrenceResult(null));

        TaskDto taskDto = taskController.close(task.getId(), new TaskCloseReopenRequest(OCCURRENCE), jwt());

        assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
    }

    @Test
    void reopenReturnsAnOccurrenceRepresentationForARecurringOccurrence() {
        Task task = recurringTask();
        when(taskMutationService.reopen(task.getId(), new com.taska.task.model.TaskCloseReopenParameters(OCCURRENCE), "account-a"))
                .thenReturn(occurrenceResult(null));

        TaskDto taskDto = taskController.reopen(task.getId(), new TaskCloseReopenRequest(OCCURRENCE), jwt());

        assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
    }

    @Test
    void everyVariantEmitsItsDiscriminatorOnTheWire() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        TaskMapperImpl taskMapper = new TaskMapperImpl();

        // kind is no longer a constructor component, so only serialization proves
        // clients can still
        // narrow the union.
        assertThat(jsonMapper.writeValueAsString(taskMapper.toNonRecurringDto(nonRecurringTask()))).contains("\"kind\":\"NON_RECURRING\"");
        Task recurringSeries = recurringTask();
        recurringSeries.setDueAt(Instant.parse("2026-05-21T17:00:00Z"));
        assertThat(jsonMapper.writeValueAsString(taskMapper.toRecurringSeriesDto(recurringSeries))).contains("\"kind\":\"RECURRING_SERIES\"")
                .doesNotContain("dueAt");
        assertThat(jsonMapper.writeValueAsString(taskMapper.toOccurrenceDto(occurrenceResult(null)))).contains("\"kind\":\"RECURRING_OCCURRENCE\"");
    }

    private RecurringTaskOccurrenceResult occurrenceResult(TaskOccurrenceState instance) {
        return new RecurringTaskOccurrenceResult(recurringTask(), instance, OCCURRENCE);
    }

    private Task nonRecurringTask() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setContent("One-off task");
        task.setIsRecurring(false);
        task.setLabels(List.of());
        return task;
    }

    private Task recurringTask() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setContent("Recurring task");
        task.setIsRecurring(true);
        task.setRecurrenceRule("FREQ=DAILY");
        task.setScheduledAt(OCCURRENCE);
        task.setLabels(List.of());
        return task;
    }

    private org.springframework.security.oauth2.jwt.Jwt jwt() {
        return org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token").header("alg", "none").subject("account-a").build();
    }
}
