package com.taska.domain.task.mcp;

import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** MCP-owned flat task output retained independently from the HTTP response union. */
public record TaskMcpOutput(
    UUID id,
    String content,
    String description,
    UUID projectId,
    UUID parentId,
    Integer order,
    Integer priority,
    List<String> labels,
    Boolean isCompleted,
    Instant scheduledAt,
    Instant dueAt,
    Boolean allDay,
    Boolean isRecurring,
    Integer estimateMinutes,
    String mentionContext,
    String recurrenceRule,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt,
    UUID instanceId,
    Instant occurrenceScheduledAt,
    Boolean isVirtual,
    Instant rruleEndsAt,
    TaskType type) {}
