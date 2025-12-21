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

    private val _dailyValue = MutableStateFlow(0.0)
    val dailyValue: StateFlow<Double> = _dailyValue

    private val _completedDaysCount = MutableStateFlow(0)
    val completedDaysCount: StateFlow<Int> = _completedDaysCount

    fun loadGoal(goalId: Int) {
        viewModelScope.launch {
            val loadedGoal = goalDao.getGoalById(goalId)
            _goal.value = loadedGoal
            if (loadedGoal != null) {
                refreshDailyValue()
                refreshCompletedDaysCount()
            }
        }
    }

    private fun refreshCompletedDaysCount() {
        val goalId = _goal.value?.id ?: return
        viewModelScope.launch {
            val count = goalDao.getCompletedDaysCount(goalId)
            _completedDaysCount.value = count
        }
    }

    private fun refreshDailyValue() {
        val goalId = _goal.value?.id ?: return
        viewModelScope.launch {
            val progress = goalDao.getDailyProgressForDate(goalId, getStartOfDay(System.currentTimeMillis()))
            _dailyValue.value = progress?.value ?: 0.0
        }
    }

    fun updateProgress(incrementValue: Double = 1.0) {
        val currentGoal = _goal.value ?: return
        val todayStart = getStartOfDay(System.currentTimeMillis())

        viewModelScope.launch {
            try {
                val existingProgress = goalDao.getDailyProgressForDate(currentGoal.id, todayStart)
                val currentDailyValue = existingProgress?.value ?: 0.0
                
                // Günlük limit kontrolü
                if (currentGoal.measurementType == "Sayisal") {
                    // Sayısal hedefler için günlük hedef kontrolü
                    if (currentDailyValue >= currentGoal.targetValue) {
                        // Günlük hedefe ulaşılmış, daha fazla artırma yapılamaz
                        return@launch
                    }
                } else {
                    // Tamamlanma hedefleri için: Zaten yapıldıysa tekrar yapma
                    if (currentDailyValue > 0) {
                        return@launch
                    }
                }
                
                val newDailyValue = if (currentGoal.measurementType == "Sayisal") {
                    // Sayısal: Günlük değeri artır
                    (currentDailyValue + incrementValue).coerceAtMost(currentGoal.targetValue)
                } else {
                    // Tamamlanma: 1.0 olarak işaretle
                    1.0
                }

                // Toplam currentValue'yu güncelle (sadece sayısal hedefler için)
                val nextTotalValue = if (currentGoal.measurementType == "Sayisal") {
                    (currentGoal.currentValue + incrementValue).coerceAtMost(currentGoal.targetValue)
                } else {
                    currentGoal.currentValue + 1.0
                }
                
                val isNowCompleted = nextTotalValue >= currentGoal.targetValue

                // Hedefi güncelle
                goalDao.updateGoal(currentGoal.copy(
                    currentValue = nextTotalValue,
                    isCompleted = isNowCompleted
                ))

                // Günlük progress'i kaydet
                goalDao.insertDailyProgress(
                    DailyProgressEntity(
                        goalId = currentGoal.id,
                        date = todayStart,
                        value = newDailyValue
                    )
                )

                // State'leri güncelle
                _dailyValue.value = newDailyValue
                _goal.value = currentGoal.copy(
                    currentValue = nextTotalValue,
                    isCompleted = isNowCompleted
                )
                // Tamamlanan gün sayısını güncelle
                refreshCompletedDaysCount()
            } catch (e: Exception) {
                android.util.Log.e("GoalDetailVM", "Progress hatası: ${e.message}")
            }
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
















