package com.esra.goaltrackproject.presentation.components.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.data.SMARTGoalEntity
import java.util.Calendar

@Composable
fun GoalListItem(
    goal: SMARTGoalEntity,
    isDoneToday: Boolean,
    dailyValue: Double,
    selectedDate: Long,
    onNavigate: () -> Unit,
    onIncrement: () -> Unit
) {
    // Gelecek günler için kontrol
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val isFutureDate = selectedDate > todayStart

    val displayValue = minOf(dailyValue, goal.targetValue)
    val progressFactor = (dailyValue / goal.targetValue).coerceIn(0.0, 1.0)
    val displayPercentage = (progressFactor * 100).toInt()
    val colorScheme = MaterialTheme.colorScheme
    val categoryIcon = getCategoryIcon(goal.category)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.background,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(categoryIcon, contentDescription = null, modifier = Modifier.padding(12.dp), tint = colorScheme.primary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                    Surface(color = colorScheme.background, shape = RoundedCornerShape(4.dp)) {
                        Text(goal.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                IconButton(
                    onClick = onIncrement,
                    enabled = !isFutureDate // Gelecek günler için devre dışı
                ) {
                    if (isFutureDate) {
                        // Gelecek günler için: Gri kilit ikonu
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Gelecek gün",
                            tint = Color.LightGray,
                            modifier = Modifier.size(28.dp)
                        )
                    } else if (goal.measurementType == "Sayisal") {
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = if (isDoneToday) "Tamamlandı" else "Artır",
                            tint = if (isDoneToday) Color(0xFF4CAF50) else colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDoneToday) Color(0xFF4CAF50) else colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (goal.measurementType == "Sayisal") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Günlük: ${displayValue.toInt()}/${goal.targetValue.toInt()} ${goal.unit ?: ""}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "%$displayPercentage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressFactor.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.background
                )
            }
        }
    }
}

@Composable
private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Genel" -> Icons.Default.GridView
        "Sağlık" -> Icons.Default.Favorite
        "İş" -> Icons.Default.Work
        "Eğitim" -> Icons.Default.School
        "Spor" -> Icons.Default.FitnessCenter
        else -> Icons.Default.GridView
    }
}

