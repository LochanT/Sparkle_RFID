package com.loyalstring.rfid.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilePickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: () -> Unit

) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Excel File") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Choose File",
                    modifier = Modifier
                        .clickable { onFileSelected() },
                    textDecoration = TextDecoration.Underline
                )
                Spacer(Modifier.height(8.dp))
                Text("Supported: XLS, XLSX", fontSize = 12.sp)
            }
        },
        confirmButton = {
            GradientButton(text = "Import", onClick = onFileSelected)
        },
        dismissButton = {
            GradientButton(text = "Cancel", onClick = onDismiss)
        }
    )
}
