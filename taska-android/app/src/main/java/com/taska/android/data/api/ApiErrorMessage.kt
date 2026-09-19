package com.taska.android.data.api

import com.google.gson.Gson
import retrofit2.HttpException

private const val DEFAULT_API_ERROR_MESSAGE = "Impossible d’enregistrer la modification"

private data class ProblemDetails(val detail: String?)

fun Throwable.toApiErrorMessage(): String {
  val problemDetail =
    (this as? HttpException)?.response()?.errorBody()?.use { errorBody ->
      runCatching {
        errorBody.charStream().use { reader ->
          Gson().fromJson(reader, ProblemDetails::class.java)?.detail
        }
      }
        .getOrNull()
    }

  return problemDetail?.takeIf(String::isNotBlank)
    ?: message?.takeIf(String::isNotBlank)
    ?: DEFAULT_API_ERROR_MESSAGE
}
