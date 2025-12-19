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
        Log.d(TAG, "=== WORKER ÇALIŞMAYA BAŞLADI ===")
        Log.d(TAG, "Worker ID: ${id}")
        Log.d(TAG, "Tags: ${tags}")
        
        val goalTitle = inputData.getString("GOAL_TITLE") ?: "Hedef Hatırlatıcısı"
        val goalId = inputData.getInt("GOAL_ID", -1)
        
        Log.d(TAG, "Bildirim gönderme işlemi başlatıldı")
        Log.d(TAG, "  - Goal ID: $goalId")
        Log.d(TAG, "  - Goal Title: $goalTitle")

        createNotificationChannel()
        val success = sendNotification(goalTitle)

        return if (success) {
            Log.d(TAG, "✅ Bildirim başarıyla gönderildi")
            Log.d(TAG, "=== WORKER BAŞARIYLA TAMAMLANDI ===")
            Result.success()
        } else {
            Log.w(TAG, "❌ Bildirim gönderilemedi - izin veya ayar sorunu olabilir")
            Log.w(TAG, "=== WORKER HATA İLE TAMAMLANDI ===")
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
            Log.d(TAG, "Bildirim kanalı oluşturuldu: $CHANNEL_ID")
        }
    }

    private fun sendNotification(title: String): Boolean {
        Log.d(TAG, "Bildirim gönderme fonksiyonu çağrıldı")
        
        // Android 13+ için izin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d(TAG, "Android 13+ izin kontrolü: ${if (hasPermission) "İZİN VAR ✅" else "İZİN YOK ❌"}")
            
            if (!hasPermission) {
                Log.w(TAG, "Bildirim izni verilmemiş (Android 13+)")
                return false
            }
        } else {
            Log.d(TAG, "Android 13 altı, izin kontrolü gerekmiyor")
        }

        // Bildirimlerin etkin olup olmadığını kontrol et
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationsEnabled = notificationManager.areNotificationsEnabled()
        Log.d(TAG, "Bildirimler sistem ayarlarında: ${if (notificationsEnabled) "AÇIK ✅" else "KAPALI ❌"}")
        
        if (!notificationsEnabled) {
            Log.w(TAG, "Bildirimler sistem ayarlarında devre dışı")
            return false
        }

        try {
            Log.d(TAG, "Bildirim oluşturuluyor...")
            Log.d(TAG, "  - Kanal ID: $CHANNEL_ID")
            Log.d(TAG, "  - Başlık: Hedefini Unutma!")
            Log.d(TAG, "  - İçerik: Zinciri kırma: $title")
            
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hedefini Unutma!")
                .setContentText("Zinciri kırma: $title")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

            val notificationId = System.currentTimeMillis().toInt()
            Log.d(TAG, "Bildirim ID: $notificationId ile gönderiliyor...")
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "✅ Bildirim başarıyla gönderildi (ID: $notificationId)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Bildirim gönderilirken hata oluştu", e)
            Log.e(TAG, "Hata detayı: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}