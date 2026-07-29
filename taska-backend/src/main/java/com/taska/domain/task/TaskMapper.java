package com.taska.domain.task;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    /**
     * Maps a {@link Task} entity to a {@link TaskDto}. The {@code position} field is exposed as
     * {@code order}. Occurrence-specific fields ({@code instanceId}, {@code scheduledAt},
     * {@code isVirtual}) are left null; use {@link #toOccurrenceDto} for recurring occurrences.
     *
     * @param task the task entity
     * @return the corresponding DTO
     */
    @Mapping(target = "order", source = "position")
    @Mapping(target = "instanceId", ignore = true)
    @Mapping(target = "scheduledAt", ignore = true)
    @Mapping(target = "isVirtual", ignore = true)
    TaskDto toDto(Task task);

    /**
     * Builds a DTO representing a single occurrence of a recurring task. The instance, when
     * non-null, may override the title, priority, due date, and completion state coming from the
     * base task. A null instance indicates a virtual (unmodified) occurrence.
     *
     * @param task        the recurring task template
     * @param instance    optional persisted instance with override values or completion status
     * @param scheduledAt the exact instant this occurrence falls on according to the RRULE
     * @return a fully populated DTO representing the occurrence
     */
    default TaskDto toOccurrenceDto(Task task, TaskInstance instance, Instant scheduledAt) {
        String content = instance != null && instance.getTitle() != null
                ? instance.getTitle() : task.getContent();
        Integer priority = instance != null && instance.getPriority() != null
                ? instance.getPriority() : task.getPriority();
        Instant dueAt = instance != null && instance.getDueAt() != null
                ? instance.getDueAt() : scheduledAt;
        boolean isCompleted = instance != null && instance.getStatus() == TaskInstanceStatus.DONE;
        Instant completedAt = instance != null ? instance.getCompletedAt() : null;
        UUID instanceId = instance != null ? instance.getId() : null;
        boolean isVirtual = instance == null;

        return new TaskDto(
                task.getId(),
                content,
                task.getDescription(),
                task.getProjectId(),
                task.getSectionId(),
                task.getParentId(),
                task.getPosition(),
                priority,
                task.getLabels(),
                isCompleted,
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
                scheduledAt,
                isVirtual,
                task.getRruleEndsAt()
        );
    }
}
