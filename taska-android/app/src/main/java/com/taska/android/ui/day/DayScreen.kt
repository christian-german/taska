package com.taska.android.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.ui.shared.RecurrenceScopeDialog
import com.taska.android.ui.shared.isAppointmentTask
import com.taska.android.ui.shared.taskTypeAccessibilityLabel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val AppBackground = Color(0xFFF6F8FA)
private val TextPrimary = Color(0xFF17233D)
private val TextSecondary = Color(0xFF78828F)
private val DividerColor = Color(0xFFD9E1E8)
private val TaskBlockDefault = Color(0xFF5B7FA6)
private val CurrentTimeRed = Color(0xFFDD4433)

private val HOUR_HEIGHT = 56.dp
private val TIME_GUTTER_W = 34.dp
private val DAY_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private enum class DragMode { MOVE, TOP, BOTTOM }
private data class BlockDragState(val blockId: String, val mode: DragMode, val deltaY: Float)

@Composable
fun DayScreen(
    viewModel: DayViewModel,
    onTaskClick: (taskId: String, scheduledAt: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val todayStr = remember { DAY_FMT.format(Calendar.getInstance().time) }

    var currentMinutes by remember {
        val c = Calendar.getInstance()
        mutableIntStateOf(c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE))
    }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            val c = Calendar.getInstance()
            currentMinutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            scrollState.scrollTo(with(density) { (HOUR_HEIGHT * 8).roundToPx() })
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .pointerInput(viewModel) {
                val edgePx = 60.dp.toPx()
                val minSwipePx = 80.dp.toPx()
                awaitEachGesture {
                    var startX = 0f
                    var gotDown = false
                    while (!gotDown) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val down = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                        if (down != null) { startX = down.position.x; gotDown = true }
                    }
                    var totalDx = 0f
                    var totalDy = 0f
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: break
                        totalDx += change.position.x - change.previousPosition.x
                        totalDy += change.position.y - change.previousPosition.y
                        if (!change.pressed) break
                    }
                    if (startX > edgePx && abs(totalDx) > minSwipePx && abs(totalDx) > 1.5f * abs(totalDy)) {
                        if (totalDx < 0) viewModel.nextDay() else viewModel.prevDay()
                    }
                }
            }
    ) {
        DayHeader(
            day = uiState.currentDay,
            allDayTasks = uiState.allDayTasks,
            projects = uiState.projects,
            dayOffset = uiState.dayOffset,
            onTaskClick = onTaskClick
        )
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Time gutter
            Box(modifier = Modifier.width(TIME_GUTTER_W).height(HOUR_HEIGHT * 24)) {
                for (h in 0 until 24) {
                    Text(
                        text = "%02d".format(h),
                        modifier = Modifier
                            .absoluteOffset(y = HOUR_HEIGHT * h - 6.dp)
                            .width(TIME_GUTTER_W - 3.dp),
                        textAlign = TextAlign.End,
                        style = TextStyle(fontSize = 9.sp, color = TextSecondary, fontFamily = com.taska.android.ui.theme.Archivo)
                    )
                }
            }
            Box(modifier = Modifier.width(0.5.dp).height(HOUR_HEIGHT * 24).background(DividerColor))

            // Single day column (full width)
            val isToday = uiState.dayOffset == 0
            SingleDayColumn(
                day = uiState.currentDay,
                blocks = uiState.tasks,
                projects = uiState.projects,
                currentMinutes = if (isToday) currentMinutes else -1,
                onTaskClick = onTaskClick,
                onReschedule = viewModel::requestRescheduleTask,
                modifier = Modifier.weight(1f)
            )
        }
    }

    uiState.pendingReschedule?.let {
        RecurrenceScopeDialog(
            title = "Déplacer la récurrence",
            onThisOnly = { viewModel.confirmRescheduleTask(RecurrenceScope.THIS_ONLY) },
            onFromThis = { viewModel.confirmRescheduleTask(RecurrenceScope.FROM_THIS) },
            onDismiss = { viewModel.dismissRescheduleScope() }
        )
    }
}

@Composable
private fun DayHeader(
    day: Calendar,
    allDayTasks: List<TaskDto>,
    projects: Map<String, ProjectDto>,
    dayOffset: Int,
    onTaskClick: (taskId: String, scheduledAt: String?) -> Unit
) {
    val dayLabel = when (dayOffset) {
        0 -> "Aujourd'hui"
        1 -> "Demain"
        -1 -> "Hier"
        else -> SimpleDateFormat("EEE d MMM", Locale.FRENCH).format(day.time)
            .replaceFirstChar { it.uppercase() }
    }
    val dateStr = if (dayOffset in -1..1) {
        SimpleDateFormat("EEE d MMM", Locale.FRENCH).format(day.time)
    } else null

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = TIME_GUTTER_W + 4.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayLabel,
                style = TextStyle(
                    fontFamily = com.taska.android.ui.theme.Archivo,
                    fontStyle = FontStyle.Italic,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
            )
            if (dateStr != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = dateStr,
                    style = TextStyle(fontFamily = com.taska.android.ui.theme.Archivo, fontSize = 11.sp, color = TextSecondary)
                )
            }
        }

        // All-day tasks row
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 24.dp)
                .padding(vertical = 2.dp)
        ) {
            Spacer(modifier = Modifier.width(TIME_GUTTER_W + 1.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 2.dp)) {
                allDayTasks.take(5).forEach { task ->
                    val color = task.projectId?.let { projects[it]?.color?.let { c -> parseHexColor(c) } }
                        ?: TaskBlockDefault
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 1.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color.copy(alpha = 0.85f))
                            .clickable { onTaskClick(task.id, task.scheduledAt) }
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAppointmentTask(task.type)) {
                                Icon(Icons.Outlined.CalendarToday, taskTypeAccessibilityLabel(task.type),
                                    tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(2.dp))
                            }
                            Text(task.content, style = TextStyle(fontSize = 9.sp, color = Color.White),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (allDayTasks.size > 5) {
                    Text(text = "+${allDayTasks.size - 5}", style = TextStyle(fontSize = 8.sp, color = TextSecondary))
                }
            }
        }
    }
}

@Composable
private fun SingleDayColumn(
    day: Calendar,
    blocks: List<DayTaskBlock>,
    projects: Map<String, ProjectDto>,
    currentMinutes: Int,
    onTaskClick: (taskId: String, scheduledAt: String?) -> Unit,
    onReschedule: (task: TaskDto, newDueAt: String, newEstimateMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragState by remember { mutableStateOf<BlockDragState?>(null) }

    BoxWithConstraints(modifier = modifier.height(HOUR_HEIGHT * 24)) {
        val dayWidth: Dp = maxWidth
        val density = LocalDensity.current
        val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }

        // Hour dividers
        for (h in 0..23) {
            HorizontalDivider(modifier = Modifier.absoluteOffset(y = HOUR_HEIGHT * h), color = DividerColor, thickness = 0.5.dp)
        }

        // Current time indicator
        if (currentMinutes >= 0) {
            val lineY = HOUR_HEIGHT * currentMinutes / 60f
            Box(modifier = Modifier.absoluteOffset(x = (-4).dp, y = lineY - 3.dp).size(6.dp).background(CurrentTimeRed, CircleShape))
            Box(modifier = Modifier.absoluteOffset(x = 2.dp, y = lineY - 0.5.dp).fillMaxWidth().height(1.dp).background(CurrentTimeRed))
        }

        val sortedBlocks = blocks.sortedBy { if (dragState?.blockId == it.task.id) 1 else 0 }

        sortedBlocks.forEach { block ->
            val ds = dragState?.takeIf { it.blockId == block.task.id }
            val project = block.task.projectId?.let { projects[it] }
            val blockColor = project?.color?.let { parseHexColor(it) } ?: TaskBlockDefault

            val (effectiveStartMin, effectiveDuration) = when {
                ds == null -> block.startMin to (block.endMin - block.startMin)
                ds.mode == DragMode.MOVE -> {
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    (block.startMin + delta).coerceIn(0, 23 * 60) to (block.endMin - block.startMin)
                }
                ds.mode == DragMode.TOP -> {
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    val newStart = (block.startMin + delta).coerceIn(0, block.endMin - 15)
                    newStart to (block.endMin - newStart)
                }
                else -> {
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    block.startMin to (block.endMin + delta - block.startMin).coerceAtLeast(15).coerceAtMost(24 * 60 - block.startMin)
                }
            }

            val blockX = dayWidth * block.col / block.totalCols + 1.dp
            val blockW = (dayWidth / block.totalCols - 2.dp).coerceAtLeast(4.dp)
            val blockY = HOUR_HEIGHT * effectiveStartMin / 60f
            val blockH = (HOUR_HEIGHT * effectiveDuration / 60f).coerceAtLeast(HOUR_HEIGHT * 0.4f)
            val isDragging = ds != null

            Box(
                modifier = Modifier
                    .absoluteOffset(x = blockX, y = blockY)
                    .width(blockW)
                    .height(blockH)
                    .clip(RoundedCornerShape(3.dp))
                    .background(blockColor.copy(alpha = if (isDragging) 0.95f else 0.85f))
                    .clickable { onTaskClick(block.task.id, block.task.scheduledAt) }
                    .pointerInput(block.task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startOffset ->
                                val zoneH = size.height / 4f
                                dragState = BlockDragState(
                                    blockId = block.task.id,
                                    mode = when {
                                        startOffset.y < zoneH -> DragMode.TOP
                                        startOffset.y > size.height - zoneH -> DragMode.BOTTOM
                                        else -> DragMode.MOVE
                                    },
                                    deltaY = 0f
                                )
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragState = dragState?.copy(deltaY = (dragState?.deltaY ?: 0f) + dragAmount.y)
                            },
                            onDragEnd = {
                                val finalDs = dragState
                                if (finalDs != null) {
                                    val deltaMin = (finalDs.deltaY / hourHeightPx * 60f).roundToInt()
                                    val (newDueAt, newDuration) = when (finalDs.mode) {
                                        DragMode.MOVE -> {
                                            val newStart = snapToQuarter((block.startMin + deltaMin).coerceIn(0, 23 * 60))
                                            formatDueAt(day, newStart) to (block.endMin - block.startMin)
                                        }
                                        DragMode.TOP -> {
                                            val newStart = snapToQuarter((block.startMin + deltaMin).coerceIn(0, block.endMin - 15))
                                            formatDueAt(day, newStart) to (block.endMin - newStart)
                                        }
                                        DragMode.BOTTOM -> {
                                            val newEnd = snapToQuarter((block.endMin + deltaMin).coerceAtLeast(block.startMin + 15).coerceAtMost(24 * 60))
                                            formatDueAt(day, block.startMin) to (newEnd - block.startMin)
                                        }
                                    }
                                    onReschedule(block.task, newDueAt, newDuration.coerceAtLeast(15))
                                }
                                dragState = null
                            },
                            onDragCancel = { dragState = null }
                        )
                    }
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.35f)))
                Text(
                    text = block.task.content,
                    modifier = Modifier.padding(start = 3.dp, end = 3.dp, top = 4.dp, bottom = 2.dp),
                    style = TextStyle(fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Medium, lineHeight = 12.sp),
                    maxLines = if (blockH >= HOUR_HEIGHT * 0.7f) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (isAppointmentTask(block.task.type)) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        taskTypeAccessibilityLabel(block.task.type),
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(11.dp)
                    )
                }
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.35f)))
            }
        }
    }
}

private fun snapToQuarter(min: Int): Int = ((min.toFloat() / 15f).roundToInt() * 15).coerceIn(0, 23 * 60)

private fun formatDueAt(day: Calendar, startMin: Int): String {
    val cal = Calendar.getInstance().apply {
        set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH),
            startMin / 60, startMin % 60, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return java.time.Instant.ofEpochMilli(cal.timeInMillis).toString()
}

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (_: Exception) { TaskBlockDefault }
