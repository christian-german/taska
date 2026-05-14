package com.taska.android.data.model

data class TaskRequest(
    val content: String,
    val description: String? = null,
    val projectId: String? = null,
    val parentId: String? = null,
    val priority: Int? = null,
    val labels: List<String>? = null,
    val dueDate: String? = null,
    val estimateMinutes: Int? = null
)
