package com.loyalstring.rfid.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MappingDialogWrapper(
    excelColumns: List<String>,
    bulkItemFields: List<String>,
    onDismiss: () -> Unit,
    onImport: (Map<String, String>) -> Unit
) {
    val mapping = remember { mutableStateMapOf<String, String>() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TableMappingScreen(
                excelColumns = excelColumns,
                bulkItemFields = bulkItemFields,
                onDismiss = onDismiss,
                onImport = onImport
            )
        }
    }
}
