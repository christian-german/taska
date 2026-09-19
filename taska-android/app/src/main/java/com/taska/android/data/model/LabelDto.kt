package com.taska.android.data.model

data class LabelDto(
  val id: String,
  val name: String,
  val color: String?,
  val order: Int?,
  val isFavorite: Boolean?,
)
