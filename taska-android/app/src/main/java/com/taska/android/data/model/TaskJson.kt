package com.taska.android.data.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

object TaskJson {
  val gson: Gson =
    GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(TaskDto::class.java, TaskDtoDeserializer())
      .create()
}

private class TaskDtoDeserializer : JsonDeserializer<TaskDto> {
  override fun deserialize(
    json: JsonElement,
    typeOfT: Type,
    context: JsonDeserializationContext,
  ): TaskDto {
    val kindValue =
      json.asJsonObject.get("kind")?.takeUnless(JsonElement::isJsonNull)?.asString
        ?: throw JsonParseException("Task representation is missing required kind")
    return when (kindValue) {
      TaskRepresentationKind.NON_RECURRING.name ->
        context.deserialize(json, NonRecurringTaskDto::class.java)
      TaskRepresentationKind.RECURRING_SERIES.name ->
        context.deserialize(json, RecurringTaskSeriesDto::class.java)
      TaskRepresentationKind.RECURRING_OCCURRENCE.name ->
        context.deserialize(json, RecurringTaskOccurrenceDto::class.java)
      else -> throw JsonParseException("Unsupported task representation kind: $kindValue")
    }
  }
}
