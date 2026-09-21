package com.taska.task.adapter.http;

import com.taska.platform.config.ApiMapperConfig;
import com.taska.task.model.NonRecurringTaskResult;
import com.taska.task.model.RecurringTaskOccurrenceResult;
import com.taska.task.model.RecurringTaskSeriesResult;
import com.taska.task.model.Task;
import com.taska.task.model.TaskCloseReopenParameters;
import com.taska.task.model.TaskCreateParameters;
import com.taska.task.model.TaskDeleteParameters;
import com.taska.task.model.TaskOccurrenceUpdateParameters;
import com.taska.task.model.TaskResult;
import com.taska.task.model.TaskType;
import com.taska.task.model.TaskUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
public interface TaskMapper {

    @Mapping(target = "order", source = "position")
    @Mapping(target = "type", defaultValue = "TODO")
    NonRecurringTaskDto toNonRecurringDto(Task task);

    @Mapping(target = "order", source = "position")
    @Mapping(target = "type", defaultValue = "TODO")
    RecurringTaskSeriesDto toRecurringSeriesDto(Task task);

    default TaskDto toDto(Task task) {
        return Boolean.TRUE.equals(task.getIsRecurring()) ? toRecurringSeriesDto(task) : toNonRecurringDto(task);
    }

    default TaskDto toDto(TaskResult taskResult) {
        return switch (taskResult) {
            case NonRecurringTaskResult result -> toNonRecurringDto(result.task());
            case RecurringTaskSeriesResult result -> toRecurringSeriesDto(result.task());
            case RecurringTaskOccurrenceResult result -> toOccurrenceDto(result);
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
     * Builds the HTTP representation of one recurring occurrence. Every effective value comes from the result itself, which owns the rule deciding
     * when a persisted occurrence-state override wins over the series definition.
     *
     * @param occurrenceResult the expanded occurrence, virtual or materialized
     * @return a fully populated occurrence representation
     */
    default RecurringTaskOccurrenceDto toOccurrenceDto(RecurringTaskOccurrenceResult occurrenceResult) {
        Task task = occurrenceResult.task();
        return new RecurringTaskOccurrenceDto(
                task.getId(),
                occurrenceResult.resolvedContent(),
                task.getDescription(),
                task.getProjectId(),
                task.getParentId(),
                task.getPosition(),
                occurrenceResult.resolvedPriority(),
                task.getLabels(),
                occurrenceResult.resolvedScheduledAt(),
                occurrenceResult.resolvedDueAt(),
                task.isAllDay(),
                task.getEstimateMinutes(),
                task.getMentionContext(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getType() == null ? TaskType.TODO : task.getType(),
                task.getRecurrenceRule(),
                task.getRruleEndsAt(),
                occurrenceResult.completed(),
                occurrenceResult.completedAt(),
                occurrenceResult.occurrenceStateId(),
                occurrenceResult.occurrenceScheduledAt(),
                occurrenceResult.virtual(),
                occurrenceResult.detached());
    }
}
