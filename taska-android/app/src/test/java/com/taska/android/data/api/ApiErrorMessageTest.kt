package com.taska.android.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiErrorMessageTest {

  @Test
  fun `problem details expose their actionable detail`() {
    val exception =
      httpException(
        """{"status":400,"title":"Bad Request","detail":"Use an explicit occurrence"}"""
      )

    assertEquals("Use an explicit occurrence", exception.toApiErrorMessage())
  }

  @Test
  fun `malformed problem details fall back to the HTTP exception message`() {
    val message = httpException("not-json").toApiErrorMessage()

    assertTrue(message.contains("HTTP 400"))
  }

  @Test
  fun `non HTTP error keeps its available message`() {
    assertEquals("Connection reset", IllegalStateException("Connection reset").toApiErrorMessage())
  }

  @Test
  fun `message-less error uses a stable fallback`() {
    assertEquals("Impossible d’enregistrer la modification", Exception().toApiErrorMessage())
  }

  private fun httpException(body: String): HttpException =
    HttpException(
      Response.error<Any>(
        400,
        body.toResponseBody("application/problem+json".toMediaType()),
      )
    )
}
