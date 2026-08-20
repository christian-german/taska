package com.taska.android.widget

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.RemoteViews
import com.taska.android.R
import com.taska.android.data.model.TaskDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun overdueWidgetText(context: Context, text: CharSequence): CharSequence =
    SpannableString(text).apply {
        setSpan(ForegroundColorSpan(context.getColor(R.color.widget_overdue_ink)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

internal fun RemoteViews.setOverdueText(context: Context, viewId: Int, text: CharSequence) {
    setTextViewText(viewId, overdueWidgetText(context, text))
}

internal fun TaskDto.isOverdueWidgetTask(
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): Boolean = isCompleted != true && scheduledAt?.let { Instant.parse(it).atZone(zone).toLocalDate() < today } == true
