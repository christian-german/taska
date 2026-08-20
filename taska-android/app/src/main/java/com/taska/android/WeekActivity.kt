package com.taska.android

import android.content.Intent
import android.os.Bundle
import com.taska.android.TaskDetailActivity
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
import com.taska.android.ui.drawer.WithDrawer
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.shared.handleCalendarTaskCreated
import com.taska.android.ui.theme.TaskaTheme
import com.taska.android.ui.week.WeekScreen
import com.taska.android.ui.week.WeekViewModel

class WeekActivity : ComponentActivity() {

    private val weekViewModel: WeekViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                val addTaskViewModel: AddTaskViewModel = viewModel()
                var showAddTask by remember { mutableStateOf(false) }

                WithDrawer(
                    onInboxSelected = {
                        startActivity(Intent(this@WeekActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    },
                    onProjectSelected = { projectId ->
                        startActivity(
                            Intent(this@WeekActivity, ProjectActivity::class.java)
                                .putExtra("project_id", projectId)
                                .putExtra("nav_current", NavDestination.WEEK.name)
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WeekScreen(
                            viewModel = weekViewModel,
                            onTaskClick = { taskId, occurrenceScheduledAt ->
                                startActivity(
                                    Intent(this@WeekActivity, TaskDetailActivity::class.java).apply {
                                        putExtra("task_id", taskId)
                                        occurrenceScheduledAt?.let { putExtra("scheduled_at", it) }
                                    }
                                )
                            },
                            onSearch = { startActivity(Intent(this@WeekActivity, SearchActivity::class.java)) },
            modifier = Modifier.weight(1f)
                        )
                        BottomNavBar(
                            current = NavDestination.WEEK,
                            onNavigate = { dest ->
                                when (dest) {
                                    NavDestination.INBOX -> startActivity(
                                        Intent(this@WeekActivity, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    )
                                    NavDestination.TODAY -> startActivity(
                                        Intent(this@WeekActivity, TodayActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    )
                                    NavDestination.DAY -> startActivity(
                                        Intent(this@WeekActivity, DayActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    )
                                    NavDestination.WEEK -> Unit
                                    NavDestination.TRACKER -> startActivity(
                                        Intent(this@WeekActivity, TrackerActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    )
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
                                refreshCalendar = weekViewModel::load,
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        weekViewModel.load()
    }
}
