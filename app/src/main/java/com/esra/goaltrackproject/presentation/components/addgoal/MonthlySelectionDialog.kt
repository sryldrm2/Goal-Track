package com.esra.goaltrackproject.presentation.components.addgoal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MonthlyDayPicker(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Ayın hangi günleri?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface.copy(alpha = 0.6f))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7li grid
            modifier = Modifier.height(200.dp).padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(31) { index ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                Surface(
                    onClick = { onDayClick(dayNum) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) colorScheme.primary else colorScheme.background,
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$dayNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySelectionDialog(
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam", color = colorScheme.primary) } },
        title = { Text("Ayın Günlerini Seçin", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
        text = {
            MonthlyDayPicker(selectedDays, onDayClick)
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surface
    )
}
