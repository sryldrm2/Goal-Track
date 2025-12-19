package com.esra.goaltrackproject.presentation.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.esra.goaltrackproject.data.DailyProgressEntity
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.workers.GoalReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

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

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            try {
                val updatedGoal = currentGoal.copy(
                    currentValue = currentGoal.currentValue + 1.0
                )
                goalDao.updateGoal(updatedGoal)
                _goal.value = updatedGoal

                val dailyProgress = DailyProgressEntity(
                    goalId = currentGoal.id,
                    date = todayStart,
                    value = 1.0
                )
                goalDao.insertDailyProgress(dailyProgress)

            } catch (e: Exception) {
                android.util.Log.e("GoalDetailVM", "Veri kaydedilirken hata: ${e.message}")
            }
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

    fun updateGoal(updatedGoal: SMARTGoalEntity) {
        viewModelScope.launch {
            goalDao.updateGoal(updatedGoal)
            _goal.value = updatedGoal

            refreshReminder(updatedGoal)
        }
    }

    private fun refreshReminder(goal: SMARTGoalEntity) {
        workManager.cancelUniqueWork("reminder_goal_unique_${goal.id}")

        if (!goal.isReminderActive || goal.reminderHour == null || goal.reminderMinute == null) {
            return
        }

        val turkeyTimeZone = java.util.TimeZone.getTimeZone("Europe/Istanbul")
        val now = Calendar.getInstance(turkeyTimeZone)
        val dueCalendar = Calendar.getInstance(turkeyTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, goal.reminderHour)
            set(Calendar.MINUTE, goal.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dueCalendar.before(now)) {
            dueCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val timeDifference = dueCalendar.timeInMillis - now.timeInMillis

        val inputData = Data.Builder()
            .putString("GOAL_TITLE", goal.title)
            .putInt("GOAL_ID", goal.id)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(timeDifference, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_goal_${goal.id}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "reminder_goal_unique_${goal.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    private fun getStartOfWeekTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val weeklyProgress = _goal.flatMapLatest { goal ->
        if (goal != null) {
            goalDao.getWeeklyProgress(goal.id, getStartOfWeekTimestamp())
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
















