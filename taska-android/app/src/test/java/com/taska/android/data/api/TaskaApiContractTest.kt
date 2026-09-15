package com.taska.android.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

class TaskaApiContractTest {

  @Test
  fun taskQueriesUseBackendParameterNames() {
    val method =
      TaskaApi::class
        .java
        .getDeclaredMethod(
          "getTasks",
          String::class.java,
          Boolean::class.javaPrimitiveType,
          String::class.java,
          String::class.java,
          String::class.java,
          kotlin.coroutines.Continuation::class.java,
        )

    val queryNames =
      method.parameterAnnotations
        .flatMap { annotations -> annotations.filterIsInstance<Query>() }
        .map(Query::value)

    assertEquals(listOf("projectId", "showCompleted", "date", "from", "to"), queryNames)
  }

  @Test
  fun occurrenceAndDeletePathsMatchBackendTemplates() {
    val followingMethod =
      TaskaApi::class.java.declaredMethods.single { it.name == "updateFollowingTask" }
    val occurrenceMethod =
      TaskaApi::class.java.declaredMethods.single { it.name == "updateOccurrence" }
    val deleteMethod = TaskaApi::class.java.declaredMethods.single { it.name == "deleteTask" }

    assertEquals(
      "/tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following",
      requireNotNull(followingMethod.getAnnotation(PUT::class.java)).value,
    )
    assertEquals(
      "/tasks/{taskId}/occurrences/{occurrenceScheduledAt}",
      requireNotNull(occurrenceMethod.getAnnotation(PUT::class.java)).value,
    )
    assertEquals(
      "/tasks/{taskId}",
      requireNotNull(deleteMethod.getAnnotation(HTTP::class.java)).path,
    )
    assertTrue(
      followingMethod.parameterAnnotations
        .flatMap { annotations -> annotations.filterIsInstance<Path>() }
        .map(Path::value)
        .contains("taskId")
    )
    assertTrue(
      followingMethod.parameterAnnotations
        .flatMap { annotations -> annotations.filterIsInstance<Path>() }
        .map(Path::value)
        .contains("occurrenceScheduledAt")
    )
  }
}
