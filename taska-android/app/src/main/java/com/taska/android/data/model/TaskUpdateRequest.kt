package com.taska.android.data.model

data class TaskUpdateRequest(
    val content: String,
    val type: String,
    val description: String?,
    val projectId: String?,
    val parentId: String?,
    val order: Int,
    val priority: Int?,
    val labels: List<String>,
    val scheduledAt: String?,
    val dueAt: String?,
    val allDay: Boolean,
    val isRecurring: Boolean,
    val estimateMinutes: Int?,
    val mentionContext: String?,
    val recurrenceRule: String?,
)

data class OccurrenceUpdateRequest(
    val title: String,
    val priority: Int?,
    val scheduledAt: String?,
    val dueAt: String?,
)

fun TaskDto.toTaskUpdateRequest() = TaskUpdateRequest(
    content = content,
    type = type ?: "TODO",
    description = description,
    projectId = projectId,
    parentId = parentId,
    order = order ?: 0,
    priority = priority,
    labels = labels ?: emptyList(),
    scheduledAt = scheduledAt,
    dueAt = dueAt,
    allDay = allDay,
    isRecurring = isRecurring == true,
    estimateMinutes = estimateMinutes,
    mentionContext = mentionContext,
    recurrenceRule = recurrenceRule,
)
