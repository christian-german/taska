package com.taska.android.data.model

data class ProjectDto(
  val id: String,
  val name: String,
  val color: String?,
  val parentId: String?,
  val order: Int?,
  val isFavorite: Boolean?,
  val viewStyle: String?,
  val isInboxProject: Boolean?,
  val planningCalendarId: String?,
  val createdAt: String?,
  val updatedAt: String?,
)
