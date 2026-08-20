package com.taska.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.ui.addtask.AddTaskBottomSheet
import com.taska.android.ui.addtask.AddTaskViewModel
import com.taska.android.ui.day.DayScreen
import com.taska.android.ui.day.DayViewModel
import com.taska.android.ui.drawer.WithDrawer
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.shared.handleCalendarTaskCreated
import com.taska.android.ui.theme.TaskaTheme

class DayActivity : ComponentActivity() {

    private val dayViewModel: DayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                val addTaskViewModel: AddTaskViewModel = viewModel()
                var showAddTask by remember { mutableStateOf(false) }

                WithDrawer(
                    onInboxSelected = {
                        startActivity(Intent(this@DayActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    },
                    onProjectSelected = { projectId ->
                        startActivity(
                            Intent(this@DayActivity, ProjectActivity::class.java)
                                .putExtra("project_id", projectId)
                                .putExtra("nav_current", NavDestination.DAY.name)
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DayScreen(
                            viewModel = dayViewModel,
                            onTaskClick = { taskId, occurrenceScheduledAt ->
                                startActivity(
                                    Intent(this@DayActivity, TaskDetailActivity::class.java).apply {
                                        putExtra("task_id", taskId)
                                        occurrenceScheduledAt?.let { putExtra("scheduled_at", it) }
                                    }
                                )
                            },
                            onSearch = { startActivity(Intent(this@DayActivity, SearchActivity::class.java)) },
            modifier = Modifier.weight(1f)
                        )
                        BottomNavBar(
                            current = NavDestination.DAY,
                            onNavigate = { dest ->
                                when (dest) {
                                    NavDestination.TODAY -> startActivity(Intent(this@DayActivity, TodayActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                                    NavDestination.DAY -> Unit
                                    NavDestination.WEEK -> startActivity(Intent(this@DayActivity, WeekActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                                    NavDestination.TRACKER -> startActivity(Intent(this@DayActivity, TrackerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                                    NavDestination.INBOX -> Unit
                                }
                            },
                            onAddClick = { showAddTask = true }
                        )
                    }
                }

                if (showAddTask) {
                    AddTaskBottomSheet(
                        viewModel = addTaskViewModel,
                        onDismiss = { showAddTask = false },
                        onTaskCreated = {
                            handleCalendarTaskCreated(
                                dismissTaskCreation = { showAddTask = false },
                                refreshCalendar = dayViewModel::load,
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dayViewModel.load()
    }
}
