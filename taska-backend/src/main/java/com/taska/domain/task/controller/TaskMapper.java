package com.taska.domain.task.controller;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.task.TaskType;
import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.occurrence.TaskInstanceStatus;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.NonRecurringTaskResult;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.RecurringTaskSeriesResult;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskUpdateParameters;
import java.time.Instant;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
public interface TaskMapper {

  @Mapping(target = "kind", constant = "NON_RECURRING")
  @Mapping(target = "order", source = "position")
  @Mapping(target = "type", defaultValue = "TODO")
  NonRecurringTaskDto toNonRecurringDto(Task task);

  @Mapping(target = "kind", constant = "RECURRING_SERIES")
  @Mapping(target = "order", source = "position")
  @Mapping(target = "type", defaultValue = "TODO")
  RecurringTaskSeriesDto toRecurringSeriesDto(Task task);

  default TaskDto toDto(Task task) {
    return Boolean.TRUE.equals(task.getIsRecurring())
        ? toRecurringSeriesDto(task)
        : toNonRecurringDto(task);
  }

  default TaskDto toDto(TaskResult taskResult) {
    return switch (taskResult) {
      case NonRecurringTaskResult result -> toNonRecurringDto(result.task());
      case RecurringTaskSeriesResult result -> toRecurringSeriesDto(result.task());
      case RecurringTaskOccurrenceResult result ->
          toOccurrenceDto(result.task(), result.taskInstance(), result.occurrenceScheduledAt());
    };
  }

  @Mapping(target = "position", source = "order", defaultValue = "0")
  @Mapping(target = "allDay", defaultValue = "false")
  @Mapping(target = "recurring", source = "isRecurring", defaultValue = "false")
  @Mapping(target = "type", defaultValue = "TODO")
  TaskCreateParameters toParameters(TaskCreateRequest taskCreateRequest);

  @Mapping(target = "position", source = "order")
  @Mapping(target = "recurring", source = "isRecurring")
  TaskUpdateParameters toParameters(TaskUpdateRequest taskUpdateRequest);

  TaskOccurrenceUpdateParameters toParameters(OccurrenceUpdateRequest occurrenceUpdateRequest);

  TaskDeleteParameters toParameters(TaskDeleteRequest taskDeleteRequest);

  TaskCloseReopenParameters toParameters(TaskCloseReopenRequest taskCloseReopenRequest);

  /**
   * Builds a DTO representing a single occurrence of a recurring task. The instance, when non-null,
   * may override the title, priority, planned scheduled time, and completion state coming from the
   * base task. A null instance indicates a virtual (unmodified) occurrence.
   *
   * @param task the recurring task template
   * @param instance optional persisted instance with override values or completion status
   * @param occurrenceScheduledAt the exact instant this occurrence falls on according to the RRULE
   * @return a fully populated DTO representing the occurrence
   */
  default RecurringTaskOccurrenceDto toOccurrenceDto(
      Task task, TaskInstance instance, Instant occurrenceScheduledAt) {
    String content =
        instance != null && instance.getTitle() != null ? instance.getTitle() : task.getContent();
    Integer priority =
        instance != null && instance.getPriority() != null
            ? instance.getPriority()
            : task.getPriority();
    Instant scheduledAt =
        instance != null && instance.getScheduledAt() != null
            ? instance.getScheduledAt()
            : occurrenceScheduledAt;
    Instant dueAt =
        instance != null && instance.getDueAt() != null ? instance.getDueAt() : task.getDueAt();
    boolean isCompleted = instance != null && instance.getStatus() == TaskInstanceStatus.DONE;
    Instant completedAt = instance != null ? instance.getCompletedAt() : null;
    UUID instanceId = instance != null ? instance.getId() : null;
    boolean isVirtual = instance == null;

    return new RecurringTaskOccurrenceDto(
        TaskRepresentationKind.RECURRING_OCCURRENCE,
        task.getId(),
        content,
        task.getDescription(),
        task.getProjectId(),
        task.getParentId(),
        task.getPosition(),
        priority,
        task.getLabels(),
        scheduledAt,
        dueAt,
        task.isAllDay(),
        task.getEstimateMinutes(),
        task.getMentionContext(),
        task.getCreatedAt(),
        task.getUpdatedAt(),
        task.getType() == null ? TaskType.TODO : task.getType(),
        task.getRecurrenceRule(),
        task.getRruleEndsAt(),
        isCompleted,
        completedAt,
        instanceId,
        occurrenceScheduledAt,
        isVirtual);
  }
}
