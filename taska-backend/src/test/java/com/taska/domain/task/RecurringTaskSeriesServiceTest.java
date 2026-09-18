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
import com.taska.domain.project.repository.Project;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.domain.task.service.RecurringTaskSeriesService;
import com.taska.domain.task.service.TaskOccurrenceService;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskService;
import com.taska.domain.task.service.TaskUpdateParameters;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Write operations on the structure of a recurring series: truncating it and creating the successor
 * series that carries the requested changes from one occurrence onward.
 *
 * <p>All repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class RecurringTaskSeriesServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  @Mock private TaskRecurrenceService taskRecurrenceService;
  @Mock private ProjectRepository projectRepository;
  @Mock private TaskPriorityEvaluationRepository priorityEvaluationRepository;
  @Mock private PlanningCalendarService planningCalendarService;
  @Mock private TaskOccurrenceNotificationService taskOccurrenceNotificationService;
  @Mock private TaskaProperties taskaProperties;

  private TaskService taskService;
  private TaskOccurrenceService taskOccurrenceService;
  private RecurringTaskSeriesService recurringTaskSeriesService;

  @BeforeEach
  void createServicesUnderTest() {
    taskService =
        new TaskService(
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

    recurringTaskSeriesService =
        new RecurringTaskSeriesService(
            taskService, taskOccurrenceService, taskRepository, priorityEvaluationRepository);
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

    recurringTaskSeriesService.replaceFollowing(taskId, occurrence, request);

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
  void replaceFollowing_withDueAt_rejectsBeforeSplittingOriginalSeries() {
    TaskUpdateParameters request =
        new TaskUpdateParameters(
            "Following",
            TaskType.TODO,
            null,
            null,
            null,
            0,
            null,
            List.of(),
            Instant.parse("2026-05-20T10:00:00Z"),
            Instant.parse("2026-05-21T17:00:00Z"),
            false,
            true,
            null,
            null,
            "FREQ=DAILY");

    assertThatThrownBy(
            () ->
                recurringTaskSeriesService.replaceFollowing(
                    randomId(), Instant.parse("2026-05-20T10:00:00Z"), request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recurring series");

    verifyNoInteractions(taskRepository);
  }

  @Test
  void updateSeriesFrom_withDueAt_rejectsBeforeSplittingOriginalSeries() {
    assertThatThrownBy(
            () ->
                recurringTaskSeriesService.updateSeriesFrom(
                    randomId(),
                    Instant.parse("2026-05-20T10:00:00Z"),
                    recurringPatch(Instant.parse("2026-05-21T17:00:00Z")),
                    false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recurring series");

    verifyNoInteractions(taskRepository);
  }

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

    recurringTaskSeriesService.updateSeriesFrom(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

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

    recurringTaskSeriesService.updateSeriesFrom(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

    Task clone = taskCaptor.getAllValues().get(1);
    assertThat(clone.getProjectId()).isEqualTo(projectId);
    assertThat(clone.getParentId()).isEqualTo(parentId);
  }

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

    recurringTaskSeriesService.updateSeriesFrom(
        taskId, firstOccurrence, request, request.priority() != null);

    Task savedOriginal = taskCaptor.getAllValues().getFirst();
    assertThat(savedOriginal.getRruleEndsAt())
        .isEqualTo(firstOccurrence.minus(1, ChronoUnit.SECONDS));
  }

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

    recurringTaskSeriesService.updateSeriesFrom(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

    List<Task> saved = taskCaptor.getAllValues();
    assertThat(saved.get(0).getRecurrenceRule()).isEqualTo(originalRule); // original unchanged
    assertThat(saved.get(1).getRecurrenceRule()).isEqualTo("FREQ=WEEKLY");
  }

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

    recurringTaskSeriesService.updateSeriesFrom(
        taskId, occurrenceScheduledAt, request, request.priority() != null);

    Task clone = taskCaptor.getAllValues().get(1);
    assertThat(clone.getRruleEndsAt()).isNull();
  }

  @Test
  void delete_fromThis_setsRruleEndsAtAndDoesNotDeleteTask() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    recurringTaskSeriesService.truncateSeriesFrom(taskId, occurrenceScheduledAt);

    assertThat(task.getRruleEndsAt()).isEqualTo(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    verify(taskRepository).save(task);
    verify(taskRepository, never()).delete(any());
    verify(taskOccurrenceStateRepository, never()).save(any());
  }

  @Test
  void delete_fromThis_firstOccurrence_rruleEndsAtIsOneDayBeforeScheduledAt() {
    UUID taskId = randomId();
    Task task = buildRecurringTask(taskId);
    Instant firstOccurrence = task.getScheduledAt();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    recurringTaskSeriesService.truncateSeriesFrom(taskId, firstOccurrence);

    assertThat(task.getRruleEndsAt()).isEqualTo(firstOccurrence.minus(1, ChronoUnit.SECONDS));
  }

  @Test
  void truncateSeriesFrom_detachesTheStatesItStrands() {
    UUID seriesId = randomId();
    Task series = buildRecurringTask(seriesId);
    Instant cut = Instant.parse("2026-05-20T10:00:00Z");
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));

    recurringTaskSeriesService.truncateSeriesFrom(seriesId, cut);

    assertThat(series.getRruleEndsAt()).isEqualTo(cut.minusSeconds(1));
    verify(taskOccurrenceStateRepository)
        .findBySeriesIdAndOccurrenceScheduledAtGreaterThanEqual(seriesId, cut);
  }

  @Test
  void updateSeriesFrom_startsTheSuccessorWhereTheCallerMovesTheRhythm() {
    UUID seriesId = randomId();
    Task series = buildRecurringTask(seriesId);
    Instant cut = Instant.parse("2026-05-20T10:00:00Z");
    Instant movedStart = Instant.parse("2026-05-19T18:00:00Z");
    when(taskRepository.findById(seriesId)).thenReturn(Optional.of(series));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    TaskPatchParameters parameters =
        new TaskPatchParameters(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            movedStart,
            null,
            null,
            null,
            null,
            null,
            null,
            RecurrenceScope.FROM_THIS,
            cut,
            null);

    TaskResult result =
        recurringTaskSeriesService.updateSeriesFrom(seriesId, cut, parameters, false);

    assertThat(result.task().getScheduledAt()).isEqualTo(movedStart);
  }
}
