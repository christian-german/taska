package com.taska.android.data.model

import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val id: String,
    val name: String,
    val color: String?,
    val parentId: String?,
    val order: Int?,
    @SerializedName("isFavorite") val isFavorite: Boolean?,
    val viewStyle: String?,
    @SerializedName("isInboxProject") val isInboxProject: Boolean?,
    val createdAt: String?,
    val updatedAt: String?
)
