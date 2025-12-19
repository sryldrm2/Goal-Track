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

class GoalReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "GOAL_CHANNEL_ID"
        private const val TAG = "GoalReminderWorker"
    }

    override suspend fun doWork(): Result {
        val goalTitle = inputData.getString("GOAL_TITLE") ?: "Hedef Hatırlatıcısı"
        
        createNotificationChannel()
        val success = sendNotification(goalTitle)

        return if (success) {
            Result.success()
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