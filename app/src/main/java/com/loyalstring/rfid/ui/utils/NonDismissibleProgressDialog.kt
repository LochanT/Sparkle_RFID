package com.loyalstring.rfid.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NonDismissibleProgressDialog(
    title: String = "Importing...",
    message: String = "Please wait while we process the Excel file."
) {
    AlertDialog(
        onDismissRequest = { /* Disable dismiss */ }, // Block back press or outside touch
        confirmButton = {}, // No buttons shown
        dismissButton = {},
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    )
}
