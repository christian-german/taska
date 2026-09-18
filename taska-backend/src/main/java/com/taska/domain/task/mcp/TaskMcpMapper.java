package com.taska.domain.task.mcp;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.task.TaskType;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.NonRecurringTaskResult;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import com.taska.domain.task.service.RecurringTaskSeriesResult;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps MCP tool inputs to the task service contract. */
@Mapper(config = ApiMapperConfig.class)
public interface TaskMcpMapper {

  /** Maps a stored non-recurring task to the flat MCP representation. */
  @Mapping(target = "order", source = "position")
  @Mapping(target = "instanceId", ignore = true)
  @Mapping(target = "occurrenceScheduledAt", ignore = true)
  @Mapping(target = "isVirtual", ignore = true)
  @Mapping(target = "isDetached", ignore = true)
  @Mapping(target = "type", defaultValue = "TODO")
  TaskMcpOutput toStoredTaskOutput(Task task);

  /** Maps a recurring-series definition while intentionally suppressing its invalid deadline. */
  @Mapping(target = "order", source = "position")
  @Mapping(target = "dueAt", ignore = true)
  @Mapping(target = "instanceId", ignore = true)
  @Mapping(target = "occurrenceScheduledAt", ignore = true)
  @Mapping(target = "isVirtual", ignore = true)
  @Mapping(target = "isDetached", ignore = true)
  @Mapping(target = "type", defaultValue = "TODO")
  TaskMcpOutput toRecurringSeriesOutput(Task task);

  /**
   * Preserves the MCP flat output shape while enforcing the series deadline invariant.
   *
   * @param task persisted task definition
   * @return flat MCP representation with a null deadline for recurring series
   */
  default TaskMcpOutput toOutput(Task task) {
    return Boolean.TRUE.equals(task.getIsRecurring())
        ? toRecurringSeriesOutput(task)
        : toStoredTaskOutput(task);
  }

  default TaskMcpOutput toOutput(TaskResult taskResult) {
    return switch (taskResult) {
      case NonRecurringTaskResult result -> toOutput(result.task());
      case RecurringTaskSeriesResult result -> toOutput(result.task());
      case RecurringTaskOccurrenceResult result -> toOccurrenceOutput(result);
    };
  }

  /**
   * Flattens one recurring occurrence for the MCP schema. The effective values come from the
   * result, which owns the override rule shared with the HTTP adapter; only the exposed shape is
   * MCP-specific.
   *
   * @param occurrenceResult the expanded occurrence, virtual or materialized
   * @return the flat MCP representation of that occurrence
   */
  default TaskMcpOutput toOccurrenceOutput(RecurringTaskOccurrenceResult occurrenceResult) {
    Task task = occurrenceResult.task();
    return new TaskMcpOutput(
        task.getId(),
        occurrenceResult.resolvedContent(),
        task.getDescription(),
        task.getProjectId(),
        task.getParentId(),
        task.getPosition(),
        occurrenceResult.resolvedPriority(),
        task.getLabels(),
        occurrenceResult.completed(),
        occurrenceResult.resolvedScheduledAt(),
        occurrenceResult.resolvedDueAt(),
        task.isAllDay(),
        true,
        task.getEstimateMinutes(),
        task.getMentionContext(),
        task.getRecurrenceRule(),
        task.getCreatedAt(),
        task.getUpdatedAt(),
        occurrenceResult.completedAt(),
        occurrenceResult.occurrenceStateId(),
        occurrenceResult.occurrenceScheduledAt(),
        occurrenceResult.virtual(),
        occurrenceResult.detached(),
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
