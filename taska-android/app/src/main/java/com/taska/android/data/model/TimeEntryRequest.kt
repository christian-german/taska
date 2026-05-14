package com.taska.android.data.model

data class TimeEntryRequest(
    val startAt: String,
    val endAt: String? = null,
    val projectId: String? = null,
    val description: String? = null,
    val notes: String? = null
)
