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
        android.util.Log.d("AddGoalViewModel", "=== HEDEF OLUŞTURMA BAŞLADI ===")
        android.util.Log.d("AddGoalViewModel", "Başlık: $title")
        android.util.Log.d("AddGoalViewModel", "Açıklama: $description")
        android.util.Log.d("AddGoalViewModel", "Hedef Değer: $targetValue")
        android.util.Log.d("AddGoalViewModel", "Hatırlatma Aktif: $isReminderActive")
        android.util.Log.d("AddGoalViewModel", "Hatırlatma Saati: $reminderHour:$reminderMinute")
        
        // --- 1. Validasyonlar ---
        if (title.isBlank()) {
            android.util.Log.w("AddGoalViewModel", "HATA: Başlık boş!")
            sendEvent(AddGoalUiEvent.ShowSnackbar("Lütfen hedefinize bir başlık yazın!"))
            return
        }

        if (targetValue <= 0) {
            android.util.Log.w("AddGoalViewModel", "HATA: Hedef değer 0 veya negatif!")
            sendEvent(AddGoalUiEvent.ShowSnackbar("Hedef değer 0'dan büyük olmalıdır!"))
            return
        }

        val currentTimeMillis = System.currentTimeMillis()
        if (endDate < currentTimeMillis - 86400000) {
            android.util.Log.w("AddGoalViewModel", "HATA: Bitiş tarihi geçmiş!")
            sendEvent(AddGoalUiEvent.ShowSnackbar("Bitiş tarihi geçmiş bir zaman olamaz!"))
            return
        }

        android.util.Log.d("AddGoalViewModel", "Validasyonlar başarılı, hedef kaydediliyor...")

        // --- 2. Kayıt İşlemi ---
        viewModelScope.launch {
            try {
                android.util.Log.d("AddGoalViewModel", "SMARTGoalEntity oluşturuluyor...")
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

                android.util.Log.d("AddGoalViewModel", "Veritabanına kaydediliyor...")
                val goalId = goalDao.insertGoal(goal)
                android.util.Log.d("AddGoalViewModel", "Hedef başarıyla kaydedildi! Goal ID: $goalId")

                // Başarılı kayıttan sonra hatırlatıcı planla
                if (isReminderActive && reminderHour != null && reminderMinute != null) {
                    android.util.Log.d("AddGoalViewModel", "Hatırlatma aktif, planlama yapılıyor...")
                    scheduleDailyReminder(goalId.toInt(), title, reminderHour, reminderMinute)
                } else {
                    android.util.Log.d("AddGoalViewModel", "Hatırlatma devre dışı veya saat bilgisi eksik")
                }

                // UI'a başarı mesajı gönder
                android.util.Log.d("AddGoalViewModel", "UI'a başarı mesajı gönderiliyor...")
                _uiEvent.send(AddGoalUiEvent.Success)
                android.util.Log.d("AddGoalViewModel", "=== HEDEF OLUŞTURMA TAMAMLANDI ===")

            } catch (e: Exception) {
                android.util.Log.e("AddGoalViewModel", "HATA: Hedef kaydedilirken exception oluştu!", e)
                _uiEvent.send(AddGoalUiEvent.ShowSnackbar("Hedef kaydedilirken bir hata oluştu."))
            }
        }
    }

    private fun sendEvent(event: AddGoalUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    private fun scheduleDailyReminder(goalId: Int, title: String, hour: Int, minute: Int) {
        android.util.Log.d("AddGoalViewModel", "--- HATIRLATMA PLANLAMA BAŞLADI ---")

        // 1. Türkiye zaman dilimini zorla ata
        val turkeyTimeZone = java.util.TimeZone.getTimeZone("Europe/Istanbul")

        // 2. Şu anki zamanı temsil eden Calendar (Türkiye saatiyle)
        val now = Calendar.getInstance(turkeyTimeZone)

        // 3. Hedef zamanı temsil eden Calendar (Türkiye saatiyle)
        val dueCalendar = Calendar.getInstance(turkeyTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // DEBUG LOGLARI (Zaman farkını net görmek için)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = turkeyTimeZone
        android.util.Log.d("AddGoalViewModel", "Gerçek Türkiye Saati: ${sdf.format(now.time)}")
        android.util.Log.d("AddGoalViewModel", "Kurulmak İstenen Saat: ${sdf.format(dueCalendar.time)}")

        // 4. Eğer hedef zaman geçmişse, bir sonraki güne planla
        if (dueCalendar.before(now)) {
            android.util.Log.d("AddGoalViewModel", "Saat geçtiği için yarına planlanıyor...")
            dueCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 5. Aradaki farkı milisaniye cinsinden hesapla
        val timeDifference = dueCalendar.timeInMillis - now.timeInMillis

        val secondsUntil = timeDifference / 1000
        val minutesUntil = secondsUntil / 60

        android.util.Log.d("AddGoalViewModel", "Hesaplanan Gecikme: $minutesUntil dakika ($timeDifference ms)")

        // WorkManager Kaydı
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

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "reminder_goal_unique_$goalId",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        android.util.Log.d("AddGoalViewModel", "✅ Bildirim $hour:$minute saatine başarıyla kuruldu.")
    }
}