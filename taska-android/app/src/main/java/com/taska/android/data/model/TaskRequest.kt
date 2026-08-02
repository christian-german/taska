package com.taska.android.data.model

data class TaskRequest(
    val content: String,
    val type: String? = null,
    val description: String? = null,
    val projectId: String? = null,
    val parentId: String? = null,
    val priority: Int? = null,
    val labels: List<String>? = null,
    val scheduledAt: String? = null,
    val dueAt: String? = null,
    val allDay: Boolean? = null,
    val estimateMinutes: Int? = null,
    val isRecurring: Boolean? = null,
    val recurrenceRule: String? = null,
    val scope: String? = null,
    val occurrenceScheduledAt: String? = null
)
