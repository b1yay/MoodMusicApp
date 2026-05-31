package com.example.moodmusicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MoodReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showMoodReminderNotification(applicationContext)
        return Result.success()
    }

    private fun showMoodReminderNotification(context: Context) {
        val channelId = "mood_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mood Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily mood music reminders"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val messages = listOf(
            "What's your mood today? 🎵",
            "Time to set the vibe! Open Arbitify",
            "Music makes everything better. Pick your mood!",
            "Your daily soundtrack is waiting ✨"
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Arbitify")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted; ignore.
        }
    }
}
