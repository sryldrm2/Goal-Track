package com.esra.goaltrackproject.presentation.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.esra.goaltrackproject.data.DailyProgressEntity
import java.util.Calendar

@Composable
fun WeeklyProgressSection(
    weeklyProgress: List<DailyProgressEntity>,
    target: Double
) {
    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(7) { index ->
            val timestamp = calendar.timeInMillis
            val dayData = weeklyProgress.find { it.date == timestamp }
            val isDone = dayData != null && dayData.value >= target

            val colorScheme = MaterialTheme.colorScheme
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (isDone) Color(0xFF4CAF50) else colorScheme.onBackground.copy(alpha = 0.05f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isDone) Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        else Text(days[index], fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}