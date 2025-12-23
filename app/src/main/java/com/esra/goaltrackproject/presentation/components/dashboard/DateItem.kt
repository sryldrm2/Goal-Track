package com.esra.goaltrackproject.presentation.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DateItem(
    day: String,
    num: String,
    isSelected: Boolean,
    primary: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp).clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) primary else colorScheme.surface,
            border = if (!isSelected) BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.2f)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = num, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) colorScheme.onSecondary else colorScheme.onSurface)
            }
        }

        // Seçili günün altındaki nokta
        if (isSelected) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(6.dp).background(primary, CircleShape))
        }
    }
}