package com.esra.goaltrackproject.presentation.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalDetailViewModel(
    application: Application,
    private val goalDao: GoalDao
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _goal = MutableStateFlow<SMARTGoalEntity?>(null)
    val goal: StateFlow<SMARTGoalEntity?> = _goal

    fun loadGoal(goalId: Int) {
        viewModelScope.launch {
            _goal.value = goalDao.getGoalById(goalId)
        }
    }

    fun markTodayCompleted() {
        val currentGoal = _goal.value ?: return

        val updatedGoal = currentGoal.copy(
            currentValue = currentGoal.currentValue + 1.0
        )

        viewModelScope.launch {
            goalDao.updateGoal(updatedGoal)
            _goal.value = updatedGoal
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            try {
                workManager.cancelUniqueWork("reminder_goal_unique_$goalId")
                goalDao.deleteGoal(goalId)
            } catch (e: Exception) {
                println("Hedef silinirken hata oluştu: $e")
            }
        }
    }
}