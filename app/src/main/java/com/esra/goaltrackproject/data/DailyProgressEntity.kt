package com.esra.goaltrackproject.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_progress",
    indices = [Index(value = ["goalId", "date"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val date: Long,
    val value: Double
)