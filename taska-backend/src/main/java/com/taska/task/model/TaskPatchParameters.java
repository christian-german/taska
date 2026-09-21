package com.taska.task.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transport-independent optional changes accepted by the existing MCP update tool.
 */
public record TaskPatchParameters(String content, String description, UUID projectId, UUID parentId, Integer position, Integer priority,
        List<String> labels, Instant scheduledAt, Instant dueAt, Boolean allDay, Boolean recurring, Integer estimateMinutes, String mentionContext,
        String recurrenceRule, RecurrenceScope scope, Instant occurrenceScheduledAt, TaskType type) {
}
