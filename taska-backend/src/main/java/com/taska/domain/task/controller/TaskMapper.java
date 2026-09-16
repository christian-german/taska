package com.taska.domain.task.controller;

import com.taska.domain.task.repository.Task;
import com.taska.domain.task.TaskType;
import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.occurrence.TaskInstanceStatus;
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
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

  /**
   * Maps a {@link Task} entity to a {@link TaskDto}. The {@code position} field is exposed as
   * {@code order}. Occurrence-specific fields ({@code instanceId}, {@code occurrenceScheduledAt},
   * {@code isVirtual}) are left null; use {@link #toOccurrenceDto} for recurring occurrences.
   *
   * @param task the task entity
   * @return the corresponding DTO
   */
  @Mapping(target = "order", source = "position")
  @Mapping(target = "instanceId", ignore = true)
  @Mapping(target = "occurrenceScheduledAt", ignore = true)
  @Mapping(target = "isVirtual", ignore = true)
  TaskDto toDto(Task task);

  default TaskDto toDto(TaskResult taskResult) {
    if (taskResult.occurrenceScheduledAt() == null) {
      return toDto(taskResult.task());
    }
    return toOccurrenceDto(
        taskResult.task(), taskResult.taskInstance(), taskResult.occurrenceScheduledAt());
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
  default TaskDto toOccurrenceDto(Task task, TaskInstance instance, Instant occurrenceScheduledAt) {
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

    return new TaskDto(
        task.getId(),
        content,
        task.getDescription(),
        task.getProjectId(),
        task.getParentId(),
        task.getPosition(),
        priority,
        task.getLabels(),
        isCompleted,
        scheduledAt,
        dueAt,
        task.isAllDay(),
        true,
        task.getEstimateMinutes(),
        task.getMentionContext(),
        task.getRecurrenceRule(),
        task.getCreatedAt(),
        task.getUpdatedAt(),
        completedAt,
        instanceId,
        occurrenceScheduledAt,
        isVirtual,
        task.getRruleEndsAt(),
        task.getType() == null ? TaskType.TODO : task.getType());
  }
}
