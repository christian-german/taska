package com.taska.android.widget

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class WidgetTextStyle(val overdue: Boolean)

internal fun widgetTaskTextStyle(
    scheduledAt: String,
    isCompleted: Boolean?,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
): WidgetTextStyle = WidgetTextStyle(
    overdue = isCompleted != true && Instant.parse(scheduledAt).atZone(zone).toLocalDate() < today,
)

internal fun styledWidgetText(text: String, style: WidgetTextStyle, overdueColor: Int): CharSequence {
    if (!style.overdue) return text
    return SpannableString(text).apply {
        setSpan(ForegroundColorSpan(overdueColor), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
