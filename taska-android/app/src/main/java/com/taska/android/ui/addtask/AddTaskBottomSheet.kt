@file:OptIn(ExperimentalMaterial3Api::class)

package com.taska.android.ui.addtask

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.taska.android.data.model.ProjectDto
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

private val Orange = Color(0xFFE8763A)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF9A9A9A)
private val ChipBorder = Color(0xFFCCCCCC)
private val ChipSelectedBg = Color(0xFF1A1A1A)
private val DisabledIcon = Color(0xFFCCCCCC)

@Composable
fun AddTaskBottomSheet(
    viewModel: AddTaskViewModel,
    onDismiss: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDateShortcuts by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showProjectPicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }
    var showPriorityPicker by remember { mutableStateOf(false) }
    var showRecurrencePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "NOUVELLE TÂCHE",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            )

            Spacer(Modifier.height(10.dp))

            BasicTextField(
                value = state.content,
                onValueChange = viewModel::updateContent,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 26.sp,
                    color = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { inner ->
                    Box {
                        if (state.content.isEmpty()) {
                            Text(
                                text = "Nouvelle tâche...",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 26.sp,
                                    color = Color(0xFFBBBBBB)
                                )
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "puis \"@demain 14h #DCM /focus 1h\" pour tout définir d'un coup",
                style = TextStyle(fontSize = 12.sp, color = Color(0xFFBBBBBB))
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskChip(
                    icon = Icons.Outlined.CalendarToday,
                    text = state.dueDateMillis?.let { formatDisplayDate(it) } ?: "Date",
                    selected = state.dueDateMillis != null,
                    onClick = { showDateShortcuts = true }
                )
                if (state.dueDateMillis != null) {
                    TaskChip(
                        icon = Icons.Outlined.Schedule,
                        text = state.dueTimeMinutes?.let { formatTimeMinutes(it) } ?: "Heure",
                        selected = state.dueTimeMinutes != null,
                        onClick = { showTimePicker = true }
                    )
                }
                TaskChip(
                    icon = Icons.Outlined.FolderOpen,
                    text = state.selectedProject?.name ?: "Projet",
                    selected = state.selectedProject != null,
                    onClick = { showProjectPicker = true }
                )
                TaskChip(
                    icon = Icons.Outlined.Timer,
                    text = state.estimateMinutes?.let { formatDuration(it) } ?: "Durée",
                    selected = state.estimateMinutes != null,
                    onClick = { showDurationPicker = true }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = DisabledIcon,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = "Répétition",
                    tint = if (state.recurrenceRule != null) Color(0xFFE07B39) else TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { showRecurrencePicker = true }
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Outlined.Flag,
                    contentDescription = "Priorité",
                    tint = priorityColor(state.priority),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { showPriorityPicker = true }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.createTask(onSuccess = {
                            viewModel.reset()
                            onTaskCreated()
                        })
                    },
                    enabled = state.content.isNotBlank() && !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange,
                        disabledContainerColor = Color(0xFFF5C4A8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Ajouter", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    if (showDateShortcuts) {
        DateShortcutsDialog(
            currentMillis = state.dueDateMillis,
            onSelect = { millis ->
                viewModel.updateDueDate(millis)
                showDateShortcuts = false
                showTimePicker = true
            },
            onOpenCalendar = {
                showDateShortcuts = false
                showCalendar = true
            },
            onDismiss = { showDateShortcuts = false }
        )
    }

    if (showCalendar) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDateMillis ?: todayMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDueDate(it) }
                    showCalendar = false
                    showTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initial = state.dueTimeMinutes
        val timeState = rememberTimePickerState(
            initialHour = initial?.div(60) ?: 9,
            initialMinute = initial?.rem(60) ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onConfirm = {
                viewModel.updateTime(timeState.hour * 60 + timeState.minute)
                showTimePicker = false
            },
            onAllDay = {
                viewModel.clearTime()
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        ) {
            TimePicker(state = timeState)
        }
    }

    if (showProjectPicker) {
        ProjectPickerDialog(
            projects = state.projects,
            selected = state.selectedProject,
            onSelect = { project ->
                viewModel.updateProject(project)
                showProjectPicker = false
            },
            onDismiss = { showProjectPicker = false }
        )
    }

    if (showDurationPicker) {
        DurationPickerDialog(
            selected = state.estimateMinutes,
            onSelect = { minutes ->
                viewModel.updateEstimate(minutes)
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false }
        )
    }

    if (showPriorityPicker) {
        PriorityPickerDialog(
            current = state.priority,
            onSelect = { p ->
                viewModel.updatePriority(p)
                showPriorityPicker = false
            },
            onDismiss = { showPriorityPicker = false }
        )
    }

    if (showRecurrencePicker) {
        AddTaskRecurrencePickerDialog(
            currentRule = state.recurrenceRule,
            onSelect = { rule ->
                viewModel.updateRecurrence(rule)
                showRecurrencePicker = false
            },
            onDismiss = { showRecurrencePicker = false }
        )
    }
}

@Composable
private fun TaskChip(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) ChipSelectedBg else Color.Transparent
    val contentColor = if (selected) Color.White else TextPrimary
    val border = if (selected) ChipSelectedBg else ChipBorder

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
        Text(
            text = text,
            style = TextStyle(fontSize = 13.sp, color = contentColor, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun DateShortcutsDialog(
    currentMillis: Long?,
    onSelect: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onDismiss: () -> Unit
) {
    val shortcuts = listOf(
        Triple("Aujourd'hui", Icons.Outlined.WbSunny, todayMillis()),
        Triple("Demain", Icons.Outlined.Event, tomorrowMillis()),
        Triple("Ce week-end", Icons.Outlined.CalendarViewWeek, nextSaturdayMillis()),
        Triple("Semaine prochaine", Icons.Outlined.CalendarMonth, nextMondayMillis())
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Date d'échéance",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            shortcuts.forEach { (label, icon, millis) ->
                val isSelected = currentMillis?.let { sameDay(it, millis) } == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(millis) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCalendar() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Choisir une date…")
            }
        }
    }
}

@Composable
private fun ProjectPickerDialog(
    projects: List<ProjectDto>,
    selected: ProjectDto?,
    onSelect: (ProjectDto?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Projet",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            // Inbox option (no project)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.MoveToInbox, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Inbox", modifier = Modifier.weight(1f))
                if (selected == null) {
                    Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            projects.forEach { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(project) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = project.color?.let { parseHexColor(it) } ?: Color.Gray
                    Canvas(modifier = Modifier.size(12.dp)) { drawCircle(dotColor) }
                    Spacer(Modifier.width(14.dp))
                    Text(project.name, modifier = Modifier.weight(1f))
                    if (selected?.id == project.id) {
                        Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

private val DURATIONS = listOf(15, 30, 60, 90, 120, 180)

@Composable
private fun DurationPickerDialog(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Durée estimée",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            DURATIONS.forEach { minutes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(minutes) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatDuration(minutes), modifier = Modifier.weight(1f))
                    if (selected == minutes) {
                        Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aucune durée", color = TextSecondary, modifier = Modifier.weight(1f))
                if (selected == null) {
                    Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PriorityPickerDialog(
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val levels = listOf(
        Triple(1, "Urgent", Color(0xFFE83030)),
        Triple(2, "Haute", Color(0xFFFF8C00)),
        Triple(3, "Moyenne", Color(0xFF4287F5)),
        Triple(4, "Aucune", Color(0xFFAAAAAA))
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Priorité",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            levels.forEach { (level, label, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(level) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Flag, null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    if (current == level) {
                        Icon(Icons.Filled.Check, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                if (level < 4) HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

// ---- Helpers ----

private fun priorityColor(p: Int): Color = when (p) {
    1 -> Color(0xFFE83030)
    2 -> Color(0xFFFF8C00)
    3 -> Color(0xFF4287F5)
    else -> Color(0xFFAAAAAA)
}

private fun todayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun tomorrowMillis(): Long = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun nextSaturdayMillis(): Long {
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    var days = (Calendar.SATURDAY - dow + 7) % 7
    if (days == 0) days = 7
    return cal.apply {
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun nextMondayMillis(): Long {
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    var days = (Calendar.MONDAY - dow + 7) % 7
    if (days == 0) days = 7
    return cal.apply {
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun sameDay(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().also { it.timeInMillis = a }
    val cb = Calendar.getInstance().also { it.timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

private fun formatDisplayDate(millis: Long): String {
    val sel = Calendar.getInstance().also { it.timeInMillis = millis }
    return when {
        sameDay(millis, todayMillis()) -> "Aujourd'hui"
        sameDay(millis, tomorrowMillis()) -> "Demain"
        else -> {
            val day = sel.get(Calendar.DAY_OF_MONTH)
            val month = sel.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.FRENCH) ?: ""
            "$day $month"
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}min"
        m == 0 -> "${h}h"
        else -> "${h}h${m}"
    }
}

@Composable
private fun TimePickerDialog(
    onConfirm: () -> Unit,
    onAllDay: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choisir l'heure",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            content()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onAllDay) { Text("Journée entière") }
                Row {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

private fun formatTimeMinutes(totalMinutes: Int): String {
    val h = (totalMinutes / 60).toString().padStart(2, '0')
    val m = (totalMinutes % 60).toString().padStart(2, '0')
    return "$h:$m"
}

private fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
} catch (_: Exception) { null }

private val RECURRENCE_OPTIONS = listOf(
    null      to "Pas de répétition",
    "daily"   to "Quotidien",
    "weekly"  to "Hebdomadaire",
    "monthly" to "Mensuel",
    "yearly"  to "Annuel",
)

@Composable
private fun AddTaskRecurrencePickerDialog(
    currentRule: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Répétition") },
        text = {
            Column {
                RECURRENCE_OPTIONS.forEach { (value, label) ->
                    val isSelected = when {
                        value == null -> currentRule.isNullOrEmpty()
                        else -> currentRule?.uppercase() == "FREQ=${value.uppercase()}"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = if (isSelected) Color(0xFFE07B39) else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
