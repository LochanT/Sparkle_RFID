package com.loyalstring.rfid.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 14.sp,   // 🔹 Added text size parameter
    fontWeight: FontWeight = FontWeight.Medium
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD32940), Color(0xFF5231A7))
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp) // 🔹 Slightly reduced padding
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = poppins,
            fontSize = textSize,             // ✅ Apply custom size
            fontWeight = fontWeight,         // Optional weight
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
