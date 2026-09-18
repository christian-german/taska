package com.taska.domain.task.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** HTTP representation of one expanded recurring occurrence. */
public record RecurringTaskOccurrenceDto(
    UUID id,
    String content,
    String description,
    UUID projectId,
    UUID parentId,
    Integer order,
    Integer priority,
    List<String> labels,
    Instant scheduledAt,
    Instant dueAt,
    Boolean allDay,
    Integer estimateMinutes,
    String mentionContext,
    Instant createdAt,
    Instant updatedAt,
    TaskType type,
    String recurrenceRule,
    Instant rruleEndsAt,
    Boolean isCompleted,
    Instant completedAt,
    UUID instanceId,
    Instant occurrenceScheduledAt,
    Boolean isVirtual,
    Boolean isDetached)
    implements TaskDto {

  /**
   * The type itself carries the discriminator; it is emitted so JSON consumers can narrow the
   * union, but it is not a constructor component and therefore cannot disagree with the type.
   */
  @JsonProperty("kind")
  @Override
  public TaskRepresentationKind kind() {
    return TaskRepresentationKind.RECURRING_OCCURRENCE;
  }
}
