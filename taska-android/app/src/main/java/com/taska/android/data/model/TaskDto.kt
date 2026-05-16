package com.taska.android.data.model

import com.google.gson.annotations.SerializedName

data class TaskDto(
    val id: String,
    val content: String,
    val description: String?,
    val projectId: String?,
    val sectionId: String?,
    val parentId: String?,
    val order: Int?,
    val priority: Int?,
    val labels: List<String>?,
    @SerializedName("isCompleted") val isCompleted: Boolean?,
    val dueAt: String?,
    val allDay: Boolean = false,
    @SerializedName("isRecurring") val isRecurring: Boolean?,
    val estimateMinutes: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val completedAt: String?
)
