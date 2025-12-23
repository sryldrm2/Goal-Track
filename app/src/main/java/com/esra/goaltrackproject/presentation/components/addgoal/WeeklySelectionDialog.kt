package com.esra.goaltrackproject.presentation.components.addgoal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyDayPicker(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Hangi günler?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                Surface(
                    onClick = { onDayClick(dayNum) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) colorScheme.primary else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.3f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            day,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklySelectionDialog(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam", color = colorScheme.primary) } },
        title = { Text("Günleri Seçin", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
        text = {
            WeeklyDayPicker(selectedDays, onDayClick)
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surface
    )
}