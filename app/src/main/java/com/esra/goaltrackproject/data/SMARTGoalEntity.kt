package com.esra.goaltrackproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class SMARTGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,

    val measurementType: String,
    val unit: String? = null,

    val targetValue: Double,
    val currentValue: Double = 0.0,

    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long,

    val isCompleted: Boolean = false,

    val isReminderActive: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,

    val note: String? = null
) {
    // Computed property - Room automatically ignores properties without backing fields
    val progressPercent: Int
        get() = if (targetValue > 0) {
            ((currentValue.toFloat() / targetValue.toFloat()) * 100)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
}
