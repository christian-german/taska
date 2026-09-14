package com.taska.android.data.model

data class TaskCreateRequest(
    val content: String,
    val type: String? = null,
    val description: String? = null,
    val projectId: String? = null,
    val parentId: String? = null,
    val order: Int? = null,
    val priority: Int? = null,
    val labels: List<String>? = null,
    val scheduledAt: String? = null,
    val dueAt: String? = null,
    val allDay: Boolean? = null,
    val estimateMinutes: Int? = null,
    val mentionContext: String? = null,
    val isRecurring: Boolean? = null,
    val recurrenceRule: String? = null,
)
