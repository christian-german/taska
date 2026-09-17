package com.taska.domain.task.mcp;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.task.TaskType;
import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.occurrence.TaskInstanceStatus;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.NonRecurringTaskResult;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.RecurringTaskSeriesResult;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import java.time.Instant;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps MCP tool inputs to the task service contract. */
@Mapper(config = ApiMapperConfig.class)
public interface TaskMcpMapper {

  @Mapping(target = "order", source = "position")
  @Mapping(target = "instanceId", ignore = true)
  @Mapping(target = "occurrenceScheduledAt", ignore = true)
  @Mapping(target = "isVirtual", ignore = true)
  @Mapping(target = "type", defaultValue = "TODO")
  TaskMcpOutput toOutput(Task task);

  default TaskMcpOutput toOutput(TaskResult taskResult) {
    return switch (taskResult) {
      case NonRecurringTaskResult result -> toOutput(result.task());
      case RecurringTaskSeriesResult result -> toOutput(result.task());
      case RecurringTaskOccurrenceResult result ->
          toOccurrenceOutput(result.task(), result.taskInstance(), result.occurrenceScheduledAt());
    };
  }

  default TaskMcpOutput toOccurrenceOutput(
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

    return new TaskMcpOutput(
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
        instance == null,
        task.getRruleEndsAt(),
        task.getType() == null ? TaskType.TODO : task.getType());
  }

  /** MCP creates plain todos; the tool contract exposes no task type. */
  @Mapping(target = "position", source = "order", defaultValue = "0")
  @Mapping(target = "allDay", defaultValue = "false")
  @Mapping(target = "recurring", source = "isRecurring", defaultValue = "false")
  @Mapping(target = "type", constant = "TODO")
  TaskCreateParameters toParameters(TaskMcpTools.TaskCreateInput taskCreateInput);

  /** {@code clearPriority} drives the separate priorityProvided flag, not a patched property. */
  @Mapping(target = "position", source = "order")
  @Mapping(target = "recurring", source = "isRecurring")
  @Mapping(target = "type", ignore = true)
  TaskPatchParameters toParameters(TaskMcpTools.TaskUpdateInput taskUpdateInput);
}
