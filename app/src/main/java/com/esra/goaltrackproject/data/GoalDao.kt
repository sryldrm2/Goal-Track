package com.esra.goaltrackproject.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SMARTGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SMARTGoalEntity)

    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<SMARTGoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Int): SMARTGoalEntity?

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Int)

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY endDate ASC")
    fun getActiveGoals(): Flow<List<SMARTGoalEntity>>

    @Query("SELECT COUNT(id) FROM goals WHERE isCompleted = 0")
    fun countActiveGoals(): Flow<Int>

    @Query("SELECT COUNT(id) FROM goals WHERE isCompleted = 1")
    fun countCompletedGoals(): Flow<Int>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY endDate ASC LIMIT 1")
    fun getNearestDeadlineGoal(): Flow<SMARTGoalEntity?>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 AND endDate >= :todayStart AND endDate <= :todayEnd ORDER BY id DESC")
    fun getTodayGoals(todayStart: Long, todayEnd: Long): Flow<List<SMARTGoalEntity>>
}