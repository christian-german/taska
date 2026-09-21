package com.taska.android.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun RecurrenceScopeDialog(
  title: String,
  onThisOnly: () -> Unit,
  onFromThis: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = { Text(text = "Choisir une action :") },
    confirmButton = {
      TextButton(onClick = onFromThis) {
        Text("Arrêter la série à partir d’ici", color = Color(0xFF1A1A1A))
      }
    },
    dismissButton = {
      TextButton(onClick = onThisOnly) {
        Text("Supprimer cette occurrence", color = Color(0xFF1A1A1A))
      }
    },
  )
}
