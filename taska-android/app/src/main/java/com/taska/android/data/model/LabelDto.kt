package com.taska.android.data.model

import com.google.gson.annotations.SerializedName

data class LabelDto(
    val id: String,
    val name: String,
    val color: String?,
    val order: Int?,
    @SerializedName("isFavorite") val isFavorite: Boolean?
)
