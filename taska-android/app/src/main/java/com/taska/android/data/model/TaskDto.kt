package com.taska.android.data.model

import com.google.gson.annotations.SerializedName

data class TaskDto(
    val id: String,
    val content: String,
    val type: String? = "TODO",
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
    val recurrenceRule: String? = null,
    val estimateMinutes: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val completedAt: String?,
    val instanceId: String? = null,
    val scheduledAt: String? = null,
    @SerializedName("isVirtual") val isVirtual: Boolean? = null,
    val rruleEndsAt: String? = null
)
