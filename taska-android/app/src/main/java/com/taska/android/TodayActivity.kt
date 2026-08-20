package com.taska.android

import android.Manifest
import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.taska.android.auth.AuthConfig
import com.taska.android.auth.LoginActivity
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.RegisterDeviceRequest
import com.taska.android.data.repository.DeviceRepository
import com.taska.android.ui.addtask.AddTaskBottomSheet
import com.taska.android.ui.addtask.AddTaskViewModel
import com.taska.android.ui.drawer.WithDrawer
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.theme.TaskaTheme
import com.taska.android.widget.TaskWidgetRefresh
import com.taska.android.ui.today.TodayScreen
import com.taska.android.ui.today.TodayViewModel
import kotlinx.coroutines.launch

class TodayActivity : ComponentActivity() {

    private val todayViewModel: TodayViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        todayViewModel.load()
        TaskWidgetRefresh.request(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)

        val accounts = AccountManager.get(this).getAccountsByType(AuthConfig.ACCOUNT_TYPE)
        if (accounts.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Récupération du token échouée", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Token: $token")
            lifecycleScope.launch {
                DeviceRepository().registerDevice(RegisterDeviceRequest(token))
            }
        }

        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                WithDrawer(
                    onInboxSelected = {
                        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    },
                    onProjectSelected = { projectId ->
                        startActivity(
                            Intent(this, ProjectActivity::class.java)
                                .putExtra("project_id", projectId)
                                .putExtra("nav_current", NavDestination.TODAY.name)
                        )
                    }
                ) {
                    TodayRoot(
                        todayViewModel = todayViewModel,
                        onNavigate = { dest ->
                            when (dest) {
                                NavDestination.INBOX -> startActivity(
                                    Intent(this, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.TODAY -> Unit
                                NavDestination.DAY -> startActivity(
                                    Intent(this, DayActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.WEEK -> startActivity(
                                    Intent(this, WeekActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                                NavDestination.TRACKER -> startActivity(
                                    Intent(this, TrackerActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayRoot(todayViewModel: TodayViewModel, onNavigate: (NavDestination) -> Unit) {
    val context = LocalContext.current
    val addTaskViewModel: AddTaskViewModel = viewModel()
    var showAddTask by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TodayScreen(
            viewModel = todayViewModel,
            onTaskClick = { taskId, occurrenceScheduledAt ->
                context.startActivity(
                    Intent(context, TaskDetailActivity::class.java).apply {
                        putExtra("task_id", taskId)
                        occurrenceScheduledAt?.let { putExtra("scheduled_at", it) }
                    }
                )
            },
            onSearch = { startActivity(Intent(context, SearchActivity::class.java)) },
            modifier = Modifier.weight(1f)
        )
        BottomNavBar(
            current = NavDestination.TODAY,
            onNavigate = onNavigate,
            onAddClick = { showAddTask = true }
        )
    }

    if (showAddTask) {
        AddTaskBottomSheet(
            viewModel = addTaskViewModel,
            onDismiss = { showAddTask = false },
            onTaskCreated = {
                showAddTask = false
                todayViewModel.load()
            }
        )
    }
}
