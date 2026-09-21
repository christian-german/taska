package com.taska.task.adapter.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taska.task.model.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** HTTP representation of one persisted non-recurring task. */
public record NonRecurringTaskDto(UUID id, String content, String description, UUID projectId, UUID parentId, Integer order, Integer priority,
        List<String> labels, Instant scheduledAt, Instant dueAt, Boolean allDay, Integer estimateMinutes, String mentionContext, Instant createdAt,
        Instant updatedAt, TaskType type, Boolean isCompleted, Instant completedAt) implements TaskDto {

    /**
     * The discriminator is carried by the type itself; it is emitted so JSON consumers can narrow the union, but it is not a constructor component
     * and therefore cannot disagree with the type.
     */
    @JsonProperty("kind")
    @Override
    public TaskRepresentationKind kind() {
        return TaskRepresentationKind.NON_RECURRING;
    }
}
