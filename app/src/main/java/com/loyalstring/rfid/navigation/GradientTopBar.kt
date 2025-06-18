package com.loyalstring.rfid.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.loyalstring.rfid.ui.utils.poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showCounter: Boolean = false,
    selectedCount: Int = 1,
    onCountSelected: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    navigationIcon?.let {
        TopAppBar(
            title = { Text(title, color = Color.White) },
            navigationIcon = it,
            actions = {
                actions()

                if (showCounter) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedCount.toString(),
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = poppins
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .height(500.dp)
                            .width(50.dp)
                    ) {
                        (1..30).forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count.toString(), fontFamily = poppins) },
                                onClick = {
                                    onCountSelected(count)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                    )
                )
        )
    }
}

