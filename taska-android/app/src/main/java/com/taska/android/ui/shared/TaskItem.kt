package com.taska.android.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val CheckboxBorder = Color(0xFFAAAAAA)
private val OverdueRed = Color(0xFFDD4433)
private val CompletedGreen = Color(0xFF14B37D)
private val PriorityUrgentColor = Color(0xFFE83030)
private val PriorityHighColor = Color(0xFFFF8C00)
private val PriorityMediumColor = Color(0xFF4287F5)

@Composable
fun TaskItem(
    task: TaskDto,
    project: ProjectDto?,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isCompleted = task.isCompleted == true
    var visuallyCompleted by remember(task.id) { mutableStateOf(isCompleted) }
    val scope = rememberCoroutineScope()
    val currentOnToggle by rememberUpdatedState(onToggle)

    // Explicit mutableFloatStateOf — no by-delegate, read .floatValue directly at top level
    val checkScaleState = remember(task.id) { mutableFloatStateOf(if (isCompleted) 1f else 0f) }
    val checkScale = checkScaleState.floatValue

    LaunchedEffect(isCompleted) {
        visuallyCompleted = isCompleted
        checkScaleState.floatValue = if (isCompleted) 1f else 0f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        val checkboxBorderColor = when {
            isOverdue -> OverdueRed
            task.priority == 1 -> PriorityUrgentColor
            task.priority == 2 -> PriorityHighColor
            task.priority == 3 -> PriorityMediumColor
            else -> CheckboxBorder
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .clickable {
                    if (visuallyCompleted) {
                        visuallyCompleted = false
                        checkScaleState.floatValue = 0f
                        onToggle()
                    } else {
                        visuallyCompleted = true
                        scope.launch {
                            val startMs = System.currentTimeMillis()
                            val durationMs = 400L
                            while (true) {
                                val elapsedMs = System.currentTimeMillis() - startMs
                                val progress = (elapsedMs / durationMs.toFloat()).coerceIn(0f, 1f)
                                checkScaleState.floatValue = progress
                                if (progress >= 1f) break
                                delay(16L)
                            }
                            delay(20L)
                            currentOnToggle()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Circle border: only when fully unchecked
            if (!visuallyCompleted && checkScale < 0.01f) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, checkboxBorderColor, CircleShape)
                )
            }
            // Check icon: visible as soon as scale > 0
            if (checkScale > 0f) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Terminé",
                    tint = CompletedGreen,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(checkScale)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val flagColor = priorityFlagColor(task.priority)
                if (!visuallyCompleted && flagColor != null) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = flagColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = task.content,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (visuallyCompleted) TextSecondary else TextPrimary,
                        lineHeight = 22.sp,
                        textDecoration = if (visuallyCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
            }

            if (project != null) {
                val projectColor = project.color?.let { parseHexColor(it) } ?: TextSecondary
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = projectColor) }
                    Text(text = project.name, style = TextStyle(fontSize = 13.sp, color = TextSecondary))
                }
            }

            val accentColor = if (isOverdue) OverdueRed else TextSecondary
            val dateStr = task.dueAt?.let { formatDate(it) }
            val timeStr = if (!task.allDay) task.dueAt?.let { formatTime(it) } else null
            val estimateStr = task.estimateMinutes?.let { formatEstimate(it) }

            if (dateStr != null || timeStr != null || estimateStr != null) {
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (dateStr != null) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(text = dateStr, style = TextStyle(fontSize = 13.sp, color = accentColor))
                    }
                    if (timeStr != null) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(text = timeStr, style = TextStyle(fontSize = 13.sp, color = accentColor))
                    }
                    if (estimateStr != null) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(text = estimateStr, style = TextStyle(fontSize = 13.sp, color = TextSecondary))
                    }
                }
            }
        }
    }
}

fun priorityFlagColor(priority: Int?): Color? = when (priority) {
    1 -> PriorityUrgentColor
    2 -> PriorityHighColor
    3 -> PriorityMediumColor
    else -> null
}

fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (_: Exception) { null }

private fun formatDate(isoDateTime: String): String = try {
    val instant = java.time.Instant.parse(isoDateTime)
    SimpleDateFormat("d MMM", Locale.FRENCH).format(java.util.Date.from(instant))
} catch (_: Exception) {
    try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDateTime.take(10)) ?: return isoDateTime
        SimpleDateFormat("d MMM", Locale.FRENCH).format(date)
    } catch (_: Exception) { isoDateTime }
}

private fun formatTime(isoDateTime: String): String? = try {
    val instant = java.time.Instant.parse(isoDateTime)
    SimpleDateFormat("HH:mm", Locale.FRENCH).format(java.util.Date.from(instant))
} catch (_: Exception) { null }

private fun formatEstimate(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h${minutes % 60}m"
}
