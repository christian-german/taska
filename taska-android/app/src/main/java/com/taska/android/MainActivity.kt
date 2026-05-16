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
import com.taska.android.auth.AuthConfig
import com.taska.android.auth.LoginActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import com.taska.android.ui.addtask.AddTaskBottomSheet
import com.taska.android.ui.addtask.AddTaskViewModel
import com.taska.android.ui.drawer.WithDrawer
import com.taska.android.ui.inbox.InboxScreen
import com.taska.android.ui.inbox.InboxViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.RegisterDeviceRequest
import com.taska.android.ui.shared.BottomNavBar
import com.taska.android.ui.shared.NavDestination
import com.taska.android.ui.theme.TaskaTheme

class MainActivity : ComponentActivity() {

    private val inboxViewModel: InboxViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        inboxViewModel.load()
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

        // Récupère et logue le token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Récupération du token échouée", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Token: $token")
            lifecycleScope.launch {
                RetrofitClient.api.registerDevice(RegisterDeviceRequest(token))
            }
        }

        enableEdgeToEdge()
        setContent {
            TaskaTheme {
                WithDrawer(
                    onProjectSelected = { projectId ->
                        startActivity(
                            Intent(this, ProjectActivity::class.java)
                                .putExtra("project_id", projectId)
                                .putExtra("nav_current", NavDestination.INBOX.name)
                        )
                    }
                ) {
                    InboxRoot(
                        inboxViewModel = inboxViewModel,
                        onNavigate = { dest ->
                            when (dest) {
                                NavDestination.TODAY -> startActivity(
                                    Intent(this, TodayActivity::class.java)
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
                                NavDestination.INBOX -> Unit
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxRoot(inboxViewModel: InboxViewModel, onNavigate: (NavDestination) -> Unit) {
    val context = LocalContext.current
    val addTaskViewModel: AddTaskViewModel = viewModel()
    var showAddTask by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        InboxScreen(
            viewModel = inboxViewModel,
            onTaskClick = { taskId ->
                context.startActivity(
                    Intent(context, TaskDetailActivity::class.java).apply {
                        putExtra("task_id", taskId)
                    }
                )
            },
            modifier = Modifier.weight(1f)
        )
        BottomNavBar(
            current = NavDestination.INBOX,
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
                inboxViewModel.load()
            }
        )
    }
}
