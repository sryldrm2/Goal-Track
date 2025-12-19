package com.esra.goaltrackproject.presentation.addgoal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.SMARTGoalEntity
import com.esra.goaltrackproject.workers.GoalReminderWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

sealed class AddGoalUiEvent {
    object Success : AddGoalUiEvent()
    data class ShowSnackbar(val message: String) : AddGoalUiEvent()
}

class AddGoalViewModel(
    application: Application,
    private val goalDao: GoalDao
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _uiEvent = Channel<AddGoalUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun addGoal(
        title: String,
        description: String,
        measurementType: String,
        unit: String?,
        targetValue: Double,
        endDate: Long,
        isReminderActive: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?,
        note: String? = null
    ) {
        // --- 1. Validasyonlar ---
        if (title.isBlank()) {
            sendEvent(AddGoalUiEvent.ShowSnackbar("Lütfen hedefinize bir başlık yazın!"))
            return
        }

        if (targetValue <= 0) {
            sendEvent(AddGoalUiEvent.ShowSnackbar("Hedef değer 0'dan büyük olmalıdır!"))
            return
        }

        val currentTimeMillis = System.currentTimeMillis()
        if (endDate < currentTimeMillis - 86400000) {
            sendEvent(AddGoalUiEvent.ShowSnackbar("Bitiş tarihi geçmiş bir zaman olamaz!"))
            return
        }

        // --- 2. Kayıt İşlemi ---
        viewModelScope.launch {
            try {
                val goal = SMARTGoalEntity(
                    title = title,
                    description = description,
                    measurementType = measurementType,
                    unit = unit,
                    targetValue = targetValue,
                    currentValue = 0.0,
                    startDate = System.currentTimeMillis(),
                    endDate = endDate,
                    isCompleted = false,
                    isReminderActive = isReminderActive,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    note = note
                )

                val goalId = goalDao.insertGoal(goal)

                // Başarılı kayıttan sonra hatırlatıcı planla
                if (isReminderActive && reminderHour != null && reminderMinute != null) {
                    scheduleDailyReminder(goalId.toInt(), title, reminderHour, reminderMinute)
                }

                // UI'a başarı mesajı gönder
                _uiEvent.send(AddGoalUiEvent.Success)

            } catch (e: Exception) {
                android.util.Log.e("AddGoalViewModel", "Hedef kaydedilirken hata oluştu", e)
                _uiEvent.send(AddGoalUiEvent.ShowSnackbar("Hedef kaydedilirken bir hata oluştu."))
            }
        }
    }

    private fun sendEvent(event: AddGoalUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    private fun scheduleDailyReminder(goalId: Int, title: String, hour: Int, minute: Int) {
        val turkeyTimeZone = java.util.TimeZone.getTimeZone("Europe/Istanbul")
        val now = Calendar.getInstance(turkeyTimeZone)
        val dueCalendar = Calendar.getInstance(turkeyTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dueCalendar.before(now)) {
            dueCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val timeDifference = dueCalendar.timeInMillis - now.timeInMillis

        val inputData = Data.Builder()
            .putString("GOAL_TITLE", title)
            .putInt("GOAL_ID", goalId)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(timeDifference, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_goal_$goalId")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "reminder_goal_unique_$goalId",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }
}