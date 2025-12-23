package com.esra.goaltrackproject.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esra.goaltrackproject.R
import com.esra.goaltrackproject.data.GoalDatabase
import com.esra.goaltrackproject.data.NotificationEntity
import com.esra.goaltrackproject.data.UserPreferences
import java.util.Calendar

class GoalReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "GOAL_CHANNEL_ID"
        private const val TAG = "GoalReminderWorker"
    }

    override suspend fun doWork(): Result {
        // Önce kullanıcı ayarlarında bildirimlerin açık olup olmadığını kontrol et
        val userPreferences = UserPreferences(applicationContext)
        if (!userPreferences.getNotificationsEnabled()) {
            return Result.success()
        }

        val goalId = inputData.getInt("GOAL_ID", -1)
        val goalTitle = inputData.getString("GOAL_TITLE") ?: "Hedef Hatırlatıcısı"

        val database = GoalDatabase.getDatabase(applicationContext)
        val goal = database.goalDao().getGoalById(goalId)

        if (goal != null) {
            val calendar = Calendar.getInstance()
            val todayAsPerSystem = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            if (goal.frequency == "Haftalık" || goal.frequency == "Haftalik") {
                val selectedDays = goal.selectedDays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                if (!selectedDays.contains(todayAsPerSystem)) {
                    Log.d(TAG, "Bugün hedef günü değil, bildirim atlanıyor.")
                    return Result.success()
                }
            }
        }

        createNotificationChannel()
        val success = sendNotification(goalTitle)

        return if (success) {
            try {
                database.notificationDao().insertNotification(
                    NotificationEntity(
                        goalId = goalId,
                        title = "Hedef Hatırlatıcısı",
                        message = "Zinciri kırma: $goalTitle",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG,"Veritabanına bildirim kaydedilemedi", e)
                Result.success()
            }
        } else {
            Log.w(TAG, "Bildirim gönderilemedi - izin veya ayar sorunu olabilir")
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Goal Reminders"
            val descriptionText = "Günlük hedef hatırlatmaları için bildirimler"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String): Boolean {
        // Android 13+ için izin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Bildirim izni verilmemiş (Android 13+)")
                return false
            }
        }

        // Bildirimlerin etkin olup olmadığını kontrol et
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "Bildirimler sistem ayarlarında devre dışı")
            return false
        }

        try {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hedefini Unutma!")
                .setContentText("Zinciri kırma: $title")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim gönderilirken hata oluştu", e)
            return false
        }
    }
}