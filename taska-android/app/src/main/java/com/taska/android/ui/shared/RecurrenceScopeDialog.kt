package com.taska.android.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.taska.android.data.model.RecurrenceScope

@Composable
fun RecurrenceScopeDialog(
    title: String,
    onThisOnly: () -> Unit,
    onFromThis: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = "Appliquer à :") },
        confirmButton = {
            TextButton(onClick = onFromThis) {
                Text("Cette occurrence et les suivantes", color = Color(0xFF1A1A1A))
            }
        },
        dismissButton = {
            TextButton(onClick = onThisOnly) {
                Text("Cette occurrence seulement", color = Color(0xFF1A1A1A))
            }
        }
    )
}
