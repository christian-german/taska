package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.taska.domain.priority.controller.TaskPriorityEvaluationMapper;
import com.taska.domain.priority.service.TaskPriorityEvaluationService;
import com.taska.domain.task.controller.TaskController;
import com.taska.domain.task.controller.TaskDto;
import com.taska.domain.task.controller.TaskMapperImpl;
import com.taska.domain.task.controller.TaskRepresentationKind;
import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Locks the variant each endpoint is allowed to return. The representation contract states which
 * kinds a caller may receive per route; without these assertions nothing prevents a query change
 * from leaking a series definition into a calendar listing, or an occurrence into an undated list.
 */
class TaskControllerRepresentationTest {

  private static final LocalDate DATE = LocalDate.parse("2026-05-20");
  private static final Instant OCCURRENCE = Instant.parse("2026-05-20T09:00:00Z");

  private final TaskService taskService = mock(TaskService.class);
  private final TaskMutationService taskMutationService = mock(TaskMutationService.class);
  private final TaskController taskController =
      new TaskController(
          taskService,
          taskMutationService,
          new TaskMapperImpl(),
          mock(TaskPriorityEvaluationService.class),
          mock(TaskPriorityEvaluationMapper.class));

  @Test
  void dateRangeListingReturnsNonRecurringTasksAndOccurrencesButNeverASeriesDefinition() {
    when(taskService.findOccurrencesForDateRange(DATE, DATE, false))
        .thenReturn(List.of(TaskResult.base(nonRecurringTask()), occurrenceResult(null)));

    List<TaskDto> tasks = taskController.getAll(null, null, false, DATE, null, null);

    assertThat(tasks)
        .extracting(TaskDto::kind)
        .containsExactly(
            TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_OCCURRENCE);
    assertThat(tasks)
        .extracting(TaskDto::kind)
        .doesNotContain(TaskRepresentationKind.RECURRING_SERIES);
  }

  @Test
  void openEndedDateRangeListingFollowsTheSameContractAsASingleDay() {
    when(taskService.findOccurrencesForDateRange(DATE, DATE.plusDays(6), false))
        .thenReturn(List.of(occurrenceResult(null)));

    List<TaskDto> tasks = taskController.getAll(null, null, false, null, DATE, DATE.plusDays(6));

    assertThat(tasks)
        .singleElement()
        .extracting(TaskDto::kind)
        .isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
  }

  @Test
  void undatedListingReturnsSeriesDefinitionsRatherThanExpandedOccurrences() {
    when(taskService.findAll(null, null, false))
        .thenReturn(List.of(nonRecurringTask(), recurringTask()));

    List<TaskDto> tasks = taskController.getAll(null, null, false, null, null, null);

    assertThat(tasks)
        .extracting(TaskDto::kind)
        .containsExactly(
            TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_SERIES);
  }

  @Test
  void singleTaskRetrievalReturnsTheSeriesDefinitionOfARecurringTask() {
    Task task = recurringTask();
    when(taskService.findById(task.getId())).thenReturn(task);

    assertThat(taskController.getById(task.getId()).kind())
        .isEqualTo(TaskRepresentationKind.RECURRING_SERIES);
  }

  @Test
  void subtaskListingNeverReturnsOccurrences() {
    UUID parentId = UUID.randomUUID();
    when(taskService.getSubtasks(parentId))
        .thenReturn(List.of(nonRecurringTask(), recurringTask()));

    assertThat(taskController.getSubtasks(parentId))
        .extracting(TaskDto::kind)
        .containsExactly(
            TaskRepresentationKind.NON_RECURRING, TaskRepresentationKind.RECURRING_SERIES);
  }

  @Test
  void occurrenceReplacementReturnsAnOccurrenceRepresentation() {
    Task task = recurringTask();
    TaskInstance instance = new TaskInstance();
    instance.setId(UUID.randomUUID());
    when(taskMutationService.replaceOccurrence(
            org.mockito.ArgumentMatchers.eq(task.getId()),
            org.mockito.ArgumentMatchers.eq(OCCURRENCE),
            org.mockito.ArgumentMatchers.any(TaskOccurrenceUpdateParameters.class),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(new RecurringTaskOccurrenceResult(task, instance, OCCURRENCE));

    TaskDto taskDto =
        taskController.replaceOccurrence(
            task.getId(),
            OCCURRENCE,
            new com.taska.domain.task.controller.OccurrenceUpdateRequest("Moved", null, null, null),
            jwt());

    assertThat(taskDto.kind()).isEqualTo(TaskRepresentationKind.RECURRING_OCCURRENCE);
  }

  @Test
  void everyVariantEmitsItsDiscriminatorOnTheWire() {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    TaskMapperImpl taskMapper = new TaskMapperImpl();

    // kind is no longer a constructor component, so only serialization proves clients can still
    // narrow the union.
    assertThat(jsonMapper.writeValueAsString(taskMapper.toNonRecurringDto(nonRecurringTask())))
        .contains("\"kind\":\"NON_RECURRING\"");
    assertThat(jsonMapper.writeValueAsString(taskMapper.toRecurringSeriesDto(recurringTask())))
        .contains("\"kind\":\"RECURRING_SERIES\"");
    assertThat(jsonMapper.writeValueAsString(taskMapper.toOccurrenceDto(occurrenceResult(null))))
        .contains("\"kind\":\"RECURRING_OCCURRENCE\"");
  }

  private RecurringTaskOccurrenceResult occurrenceResult(TaskInstance instance) {
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
    return org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("account-a")
        .build();
  }
}
