package com.taska.domain.task.controller;

import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Discriminated HTTP representation of a task resource or recurring occurrence. */
public sealed interface TaskDto
    permits NonRecurringTaskDto, RecurringTaskOccurrenceDto, RecurringTaskSeriesDto {

  TaskRepresentationKind kind();

  UUID id();

  String content();

  String description();

  UUID projectId();

  UUID parentId();

  Integer order();

  Integer priority();

  List<String> labels();

  Instant scheduledAt();

  Instant dueAt();

  Boolean allDay();

  Integer estimateMinutes();

  String mentionContext();

  Instant createdAt();

  Instant updatedAt();

  TaskType type();
}
