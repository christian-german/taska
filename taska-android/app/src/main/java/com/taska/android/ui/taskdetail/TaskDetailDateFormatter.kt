package com.taska.android.ui.taskdetail

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatScheduledTaskDetailDate(
    scheduledAt: String?,
    allDay: Boolean,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.FRENCH,
    today: LocalDate = LocalDate.now(zone),
): String? = scheduledAt?.let {
    formatTaskDetailDate(
        it,
        includeTime = !allDay,
        zone = zone,
        locale = locale,
        today = today,
    )
}

internal fun formatTaskDetailDate(
    value: String,
    includeTime: Boolean,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.FRENCH,
    today: LocalDate = LocalDate.now(zone),
): String = try {
    val zoned = Instant.parse(value).atZone(zone)
    val dayPart = when (zoned.toLocalDate()) {
        today -> "aujourd'hui"
        today.minusDays(1) -> "hier"
        today.plusDays(1) -> "demain"
        else -> "${zoned.dayOfMonth} ${zoned.format(DateTimeFormatter.ofPattern("MMM", locale))}"
    }

    if (includeTime) {
        "$dayPart · ${zoned.format(DateTimeFormatter.ofPattern("HH:mm", locale))}"
    } else {
        dayPart
    }
} catch (_: Exception) {
    value.substringBefore('T')
}
