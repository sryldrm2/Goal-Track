package com.esra.goaltrackproject.presentation.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esra.goaltrackproject.data.SMARTGoalEntity

@Composable
fun DayStatisticsSection(
    goal: SMARTGoalEntity,
    completedDaysCount: Int,
    totalDays: Int,
    remainingDays: Int
) {

    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Tamamlanan", completedDaysCount, "Gün", colorScheme.secondary, Modifier.weight(1f))
        StatCard("Toplam", totalDays, "Gün", colorScheme.primary, Modifier.weight(1f))
        StatCard("Kalan", remainingDays, "Gün", Color(0xFFEF4444), Modifier.weight(1f))
    }
}