package com.taska.android.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.taska.android.data.model.TaskDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val AppBackground = Color(0xFFEAE5DC)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9A9A9A)
private val DividerColor = Color(0xFFD5D0C8)
private val TaskBlockDefault = Color(0xFF5B7FA6)
private val CurrentTimeRed = Color(0xFFDD4433)

private val HOUR_HEIGHT = 56.dp
private val TIME_GUTTER_W = 34.dp
private val DAY_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val DAY_NAMES = listOf("lun", "mar", "mer", "jeu", "ven", "sam", "dim")

private enum class DragMode { MOVE, TOP, BOTTOM }
private data class BlockDragState(val blockId: String, val mode: DragMode, val deltaY: Float)

@Composable
fun WeekScreen(
    viewModel: WeekViewModel,
    onTaskClick: (String) -> Unit,
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
            .background(AppBackground)
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
                        if (totalDx < 0) viewModel.nextWeek() else viewModel.prevWeek()
                    }
                }
            }
    ) {
        WeekHeader(
            weekDays = uiState.weekDays,
            allDayTasksByDay = uiState.allDayTasksByDay,
            projects = uiState.projects,
            todayStr = todayStr,
            onTaskClick = onTaskClick
        )
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            TimeGutter()
            Box(modifier = Modifier.width(0.5.dp).height(HOUR_HEIGHT * 24).background(DividerColor))
            uiState.weekDays.forEachIndexed { index, day ->
                val isToday = DAY_FMT.format(day.time) == todayStr
                DayColumn(
                    day = day,
                    blocks = uiState.tasksByDay.getOrElse(index) { emptyList() },
                    projects = uiState.projects,
                    currentMinutes = if (isToday) currentMinutes else -1,
                    onTaskClick = onTaskClick,
                    onReschedule = viewModel::rescheduleTask,
                    modifier = Modifier.weight(1f)
                )
                if (index < 6) {
                    Box(modifier = Modifier.width(0.5.dp).height(HOUR_HEIGHT * 24).background(DividerColor))
                }
            }
        }
    }
}

@Composable
private fun TimeGutter() {
    Box(modifier = Modifier.width(TIME_GUTTER_W).height(HOUR_HEIGHT * 24)) {
        for (h in 0 until 24) {
            Text(
                text = "%02d".format(h),
                modifier = Modifier
                    .absoluteOffset(y = HOUR_HEIGHT * h - 6.dp)
                    .width(TIME_GUTTER_W - 3.dp),
                textAlign = TextAlign.End,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun WeekHeader(
    weekDays: List<Calendar>,
    allDayTasksByDay: List<List<TaskDto>>,
    projects: Map<String, ProjectDto>,
    todayStr: String,
    onTaskClick: (String) -> Unit
) {
    if (weekDays.isEmpty()) {
        Spacer(modifier = Modifier.height(60.dp))
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week label
        Text(
            text = buildWeekLabel(weekDays),
            modifier = Modifier.padding(start = TIME_GUTTER_W + 4.dp, top = 6.dp, bottom = 2.dp),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp,
                color = TextPrimary
            )
        )

        // Day name + number row
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(TIME_GUTTER_W))
            weekDays.forEachIndexed { i, day ->
                val isToday = DAY_FMT.format(day.time) == todayStr
                Column(
                    modifier = Modifier.weight(1f).padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = DAY_NAMES[i],
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = if (isToday) TextPrimary else TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    Box(
                        modifier = if (isToday) {
                            Modifier.size(26.dp).background(TextPrimary, CircleShape)
                        } else {
                            Modifier.size(26.dp)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.get(Calendar.DAY_OF_MONTH).toString(),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color.White else TextPrimary
                            )
                        )
                    }
                }
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
            allDayTasksByDay.forEachIndexed { i, tasks ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1.dp)
                ) {
                    tasks.take(3).forEach { task ->
                        val color = task.projectId?.let { projects[it]?.color?.let { c -> parseHexColor(c) } }
                            ?: TaskBlockDefault
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color.copy(alpha = 0.85f))
                                .clickable { onTaskClick(task.id) }
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = task.content,
                                style = TextStyle(fontSize = 8.sp, color = Color.White),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (tasks.size > 3) {
                        Text(
                            text = "+${tasks.size - 3}",
                            style = TextStyle(fontSize = 8.sp, color = TextSecondary)
                        )
                    }
                }
                if (i < 6) Spacer(modifier = Modifier.width(0.5.dp))
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: Calendar,
    blocks: List<TaskBlock>,
    projects: Map<String, ProjectDto>,
    currentMinutes: Int,
    onTaskClick: (String) -> Unit,
    onReschedule: (taskId: String, newDueAt: String, newEstimateMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragState by remember { mutableStateOf<BlockDragState?>(null) }

    BoxWithConstraints(modifier = modifier.height(HOUR_HEIGHT * 24)) {
        val dayWidth: Dp = maxWidth
        val density = LocalDensity.current
        val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }

        // Hour dividers
        for (h in 0..23) {
            HorizontalDivider(
                modifier = Modifier.absoluteOffset(y = HOUR_HEIGHT * h),
                color = DividerColor,
                thickness = 0.5.dp
            )
        }

        // Current time indicator
        if (currentMinutes >= 0) {
            val lineY = HOUR_HEIGHT * currentMinutes / 60f
            Box(
                modifier = Modifier
                    .absoluteOffset(x = (-4).dp, y = lineY - 3.dp)
                    .size(6.dp)
                    .background(CurrentTimeRed, CircleShape)
            )
            Box(
                modifier = Modifier
                    .absoluteOffset(x = 2.dp, y = lineY - 0.5.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CurrentTimeRed)
            )
        }

        // Render non-dragged blocks first, dragged block last (on top)
        val sortedBlocks = blocks.sortedBy { if (dragState?.blockId == it.task.id) 1 else 0 }

        sortedBlocks.forEach { block ->
            val ds = dragState?.takeIf { it.blockId == block.task.id }
            val project = block.task.projectId?.let { projects[it] }
            val blockColor = project?.color?.let { parseHexColor(it) } ?: TaskBlockDefault

            // Effective position + size based on drag state
            val (effectiveStartMin, effectiveDuration) = when {
                ds == null -> block.startMin to (block.endMin - block.startMin)
                ds.mode == DragMode.MOVE -> {
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    val newStart = (block.startMin + delta).coerceIn(0, 23 * 60)
                    newStart to (block.endMin - block.startMin)
                }
                ds.mode == DragMode.TOP -> {
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    val newStart = (block.startMin + delta).coerceIn(0, block.endMin - 15)
                    newStart to (block.endMin - newStart)
                }
                else -> { // BOTTOM
                    val delta = (ds.deltaY / hourHeightPx * 60f).roundToInt()
                    val newEnd = (block.endMin + delta).coerceAtLeast(block.startMin + 15).coerceAtMost(24 * 60)
                    block.startMin to (newEnd - block.startMin)
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
                    .clickable { onTaskClick(block.task.id) }
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
                                    onReschedule(block.task.id, newDueAt, newDuration.coerceAtLeast(15))
                                }
                                dragState = null
                            },
                            onDragCancel = { dragState = null }
                        )
                    }
            ) {
                // Top resize zone indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                )

                // Task content
                Text(
                    text = block.task.content,
                    modifier = Modifier.padding(start = 3.dp, end = 3.dp, top = 4.dp, bottom = 2.dp),
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 12.sp
                    ),
                    maxLines = if (blockH >= HOUR_HEIGHT * 0.7f) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Bottom resize zone indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}

private fun snapToQuarter(min: Int): Int =
    ((min.toFloat() / 15f).roundToInt() * 15).coerceIn(0, 23 * 60)

private fun formatDueAt(day: Calendar, startMin: Int): String =
    "%04d-%02d-%02dT%02d:%02d:00".format(
        day.get(Calendar.YEAR),
        day.get(Calendar.MONTH) + 1,
        day.get(Calendar.DAY_OF_MONTH),
        startMin / 60,
        startMin % 60
    )

private fun buildWeekLabel(weekDays: List<Calendar>): String {
    if (weekDays.isEmpty()) return ""
    val first = weekDays.first()
    val last = weekDays.last()
    val firstDay = SimpleDateFormat("d", Locale.FRENCH).format(first.time)
    val lastDay = SimpleDateFormat("d", Locale.FRENCH).format(last.time)
    val firstMonth = SimpleDateFormat("MMM", Locale.FRENCH).format(first.time)
    val lastMonth = SimpleDateFormat("MMM", Locale.FRENCH).format(last.time)
    val year = SimpleDateFormat("yyyy", Locale.US).format(last.time)
    return if (firstMonth == lastMonth) "$firstDay–$lastDay $lastMonth $year"
    else "$firstDay $firstMonth – $lastDay $lastMonth $year"
}

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (_: Exception) {
    TaskBlockDefault
}
