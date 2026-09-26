package com.rank.lexi.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rank.lexi.MainActivity
import com.rank.lexi.R
import com.rank.lexi.data.repository.GameRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StreakReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var gameRepository: GameRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            StreakReminderScheduler.scheduleDailyReminder(context)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // If today's daily game has not been completed, remind player to protect streak
                val hasCompleted = !gameRepository.hasActiveGameForToday() && gameRepository.hasSessionForToday()
                if (!hasCompleted) {
                    showStreakNotification(context)
                }
            } catch (_: Exception) {
                showStreakNotification(context)
            } finally {
                // Reschedule for next day
                StreakReminderScheduler.scheduleDailyReminder(context)
                pendingResult.finish()
            }
        }
    }

    private fun showStreakNotification(context: Context) {
        val channelId = "daily_streak_reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Streak Protection",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to maintain your Wordle winning streak"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.createChooser(this, "").flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Protect Your Daily Streak! 🔥")
            .setContentText("Today's Wordle puzzle is waiting. Don't let your streak break!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2001, notification)
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+
        }
    }
}
